package ch.goetschy.android.accounts.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import ch.goetschy.android.accounts.BuildConfig;
import ch.goetschy.android.accounts.R;
import ch.goetschy.android.accounts.contentprovider.MyAccountsContentProvider;
import ch.goetschy.android.accounts.objects.Account;
import ch.goetschy.android.accounts.objects.Filter;
import ch.goetschy.android.accounts.objects.Transaction;
import ch.goetschy.android.accounts.objects.Type;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.haarman.supertooltips.ToolTip;
import com.haarman.supertooltips.ToolTipRelativeLayout;
import com.haarman.supertooltips.ToolTipView;
import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.CustomLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.LegendAlign;
import com.jjoe64.graphview.GraphViewDataInterface;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewStyle;
import com.jjoe64.graphview.LineGraphView;

public class GraphActivity extends SherlockActivity implements
		ToolTipView.OnToolTipViewClickedListener {

	private Account mAccount;
	private Filter mFilter = null;
	private CompleteData mData = null;

	private LoadAndDisplayTask loadAndDisplayTask = null;
	private boolean firstFill = true;

	private static final int FILTER_ACTIVITY = 10;

	// graph type
	private int mGraphType = TIME_GRAPH;

	private static final int TIME_GRAPH = 0;
	private static final int TYPE_GRAPH = 1;
	private static final int TIME_AND_TYPE_GRAPH = 2;

	private ToolTipView mTooltipGeneral = null;
	private ToolTipView mTooltipTypes = null;
	private ToolTipRelativeLayout mTooltipLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_graph);

		mTooltipLayout = (ToolTipRelativeLayout) findViewById(R.id.activity_graph_tooltiplayout);

		// DATA FROM PARENT ACTIVITY
		Bundle extras = getIntent().getExtras();
		// account
		mAccount = new Account();
		if (extras != null) {
			mAccount.setUri((Uri) extras
					.getParcelable(MyAccountsContentProvider.CONTENT_ITEM_TYPE));
			mAccount.loadFromDB(getContentResolver());
			this.setTitle(mAccount.getName());
		} else {
			Toast.makeText(this, "No account uri in bundle", Toast.LENGTH_LONG)
					.show();
			setResult(RESULT_CANCELED);
			finish();
		}
		// filter
		mFilter = (Filter) extras.getSerializable(Filter.class.toString());
		if (mFilter == null) {
			Toast.makeText(this, "No uri in filter", Toast.LENGTH_LONG).show();
			setResult(RESULT_CANCELED);
			finish();
		}

		// load and display data
		loadAndDisplay(true, true);

		// ACTION BAR ------------------
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setDisplayHomeAsUpEnabled(true);
	}

	/*
	 * loads the data and/or construct the graph
	 */
	private void loadAndDisplay(boolean load, boolean display) {
		if (BuildConfig.DEBUG)
			Log.w("loadAndDisplay", "called");
		// do not execute if firstFill and no loading
		if (!load && firstFill)
			return;
		// only one task
		if (loadAndDisplayTask == null) {
			if (BuildConfig.DEBUG)
				Log.w("loadAndDisplay", "executed");
			loadAndDisplayTask = new LoadAndDisplayTask(this, load, display,
					mAccount, mFilter, mData, mGraphType);
			loadAndDisplayTask.execute();
		}
	}

	private void afterLoadAndDisplayTask(GraphView graphView,
			CompleteData completeData) {
		Log.w("loadAndDisplay", "finished");

		// update data
		mData = completeData;

		if (graphView != null) {
			// display graphView
			LinearLayout layout = (LinearLayout) findViewById(R.id.activity_graph_layout);
			layout.removeAllViews(); // clear layout
			layout.addView(graphView);
			Log.w("loadAndDisplay", "graph added to layout");
		} else {
			makeToast("No data. Change the filter.");
		}

		// unlock the item selection in actionBar
		firstFill = false;

		// set task to null
		loadAndDisplayTask = null;
	}

	private static class LoadAndDisplayTask extends AsyncTask<Void, Void, Void> {

		private ProgressDialog progressDialog;
		private GraphActivity tContext; // t for task
		private boolean tLoad, tDisplay;
		private CompleteData tCompleteData;

		private GraphView tGraphView = null;
		private int tGraphType;

		// copy all vars
		private Account tAccount;
		private Filter tFilter;

		public LoadAndDisplayTask(GraphActivity context, boolean load,
				boolean display, Account account, Filter filter,
				CompleteData data, int graphType) {
			tContext = context;
			tLoad = load;
			tDisplay = display;
			tAccount = account;
			tFilter = filter;
			tCompleteData = data;
			tGraphType = graphType;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new ProgressDialog(tContext,
					ProgressDialog.STYLE_SPINNER);
			progressDialog.setMessage("Loading...");
			progressDialog.show();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			if (BuildConfig.DEBUG)
				Log.w("graphActivity", "loadAndDisplay");

			boolean noTransaction = false;

			if (tLoad)
				noTransaction = !loadData(); // if loadData fails -> no trans

			// if no loading and no data -> also noTransaction
			if (tCompleteData == null || tCompleteData.getData().isEmpty())
				noTransaction = true;

			Log.w("display", "notrans = " + noTransaction);
			if (tDisplay && !noTransaction)
				displayGraph();

			return null;

		}

		/**
		 * create the completeData object
		 */
		private boolean loadData() {
			// get transactions
			ArrayList<Transaction> transactions = tAccount.getListTransactions(
					tContext.getContentResolver(), tFilter);

			if (BuildConfig.DEBUG)
				Log.w("graphActivity", "num of trans = " + transactions.size());
			// no trans
			if (transactions.size() == 0) {
				if (BuildConfig.DEBUG)
					Log.w("graphActivity", "num of trans = 0");
				return false;
			}

			// debug
			if (BuildConfig.DEBUG) {
				Log.w("graphActivity", "num of trans " + transactions.size());
				for (Transaction trans : transactions)
					Log.w("graphActivity", "trans " + trans.getName());
			}

			// create data object (with the different types)
			tCompleteData = new CompleteData(tFilter.getTypesList(tContext
					.getContentResolver()));

			// add each trans
			for (Transaction trans : transactions)
				tCompleteData.addTransaction(trans);

			return true;
		}

		/**
		 * construc the graph with the CompleteData object
		 */
		private void displayGraph() {
			if (BuildConfig.DEBUG)
				Log.w("display", "begin type : " + tGraphType);

			switch (tGraphType) {
			case GraphActivity.TYPE_GRAPH:
				if (BuildConfig.DEBUG)
					Log.w("display", "begin type graph");
				// create an anonymous inner class to have a new namespace
				new Object() {
					void execute() {
						// init graphview
						tGraphView = new BarGraphView(tContext // context
								, "Amount per type"// heading
						);
						// init graphViewData
						ArrayList<GraphViewData> graphViewDataList = new ArrayList<GraphViewData>();

						// construct data ----------------
						// get the full data array
						ArrayList<ArrayList<CompleteDataPoint>> dataArray = tCompleteData
								.getData();
						int numberOfTypes = tCompleteData.getNumberOfTypes(); // number
																				// of
																				// types
						double amountsPerType[] = new double[numberOfTypes];
						if (BuildConfig.DEBUG)
							Log.w("display", "number of types " + numberOfTypes);
						for (ArrayList<CompleteDataPoint> column : dataArray) { // compute
							// each
							// graph
							// point
							for (int i = 0; i < numberOfTypes; i++) {
								CompleteDataPoint cell = column.get(i);
								amountsPerType[i] += cell.getValue();
							}
						}

						// create the graph points
						for (int i = 0; i < numberOfTypes; i++) {
							graphViewDataList.add(new GraphViewData(i,
									amountsPerType[i]));
							if (BuildConfig.DEBUG)
								Log.w("display", "type " + i + " -> "
										+ amountsPerType[i]);
						}

						// convert data in data series
						GraphViewData[] graphViewDataArray = graphViewDataList
								.toArray(new GraphViewData[graphViewDataList
										.size()]);

						// add series to graph
						GraphViewSeries dataSeries = new GraphViewSeries(
								graphViewDataArray);
						tGraphView.addSeries(dataSeries);

						// date labels
						tGraphView
								.setCustomLabelFormatter(new CustomLabelFormatter() {
									@Override
									public String formatLabel(double value,
											boolean isValueX) {
										if (isValueX) {
											// return the type
											return tCompleteData
													.getTypeLabels().get(
															(int) value);
										}
										return null; // let graphview generate
														// Y-axis label for
														// us
									}
								});

						// scroll and scale on
						tGraphView.setViewPort(0, numberOfTypes - 1); // full
																		// viewPort
						tGraphView.setScrollable(true);
						tGraphView.setScalable(true);
					}
				}.execute();
				if (BuildConfig.DEBUG)
					Log.w("display", "end type graph");

				break;

			case GraphActivity.TIME_GRAPH:
				if (BuildConfig.DEBUG)
					Log.w("display", "begin time graph");
				new Object() {
					void execute() {
						// init graphview
						tGraphView = new LineGraphView(tContext // context
								, "Evolution in time"// heading
						);
						// init graphViewData
						ArrayList<GraphViewData> graphViewDataList = new ArrayList<GraphViewData>();

						// construct data ----------------
						ArrayList<ArrayList<CompleteDataPoint>> dataArray = tCompleteData
								.getData();
						int dataArraySize = dataArray.size();
						// get amount
						double amount = tAccount.getAmountUpTo(
								tContext.getContentResolver(),
								tCompleteData.getLowerDate());
						if (BuildConfig.DEBUG)
							Log.w("display", "data size " + dataArraySize);
						for (int i = 0; i < dataArraySize; i++) { // compute
																	// each
																	// graph
																	// point
							ArrayList<CompleteDataPoint> column = dataArray
									.get(i);
							for (CompleteDataPoint cell : column) {
								amount += cell.getValue();
							}
							graphViewDataList.add(new GraphViewData(i, amount));
						}

						// convert data in data series
						GraphViewData[] graphViewDataArray = graphViewDataList
								.toArray(new GraphViewData[graphViewDataList
										.size()]);

						// add series to graph
						GraphViewSeries dataSeries = new GraphViewSeries(
								graphViewDataArray);
						tGraphView.addSeries(dataSeries);

						// date labels
						tGraphView
								.setCustomLabelFormatter(new CustomLabelFormatter() {
									@Override
									public String formatLabel(double value,
											boolean isValueX) {
										if (isValueX) {
											// return the date string
											return Filter
													.millisToText(tCompleteData
															.getTimeLabels()
															.get((int) value));
										}
										return null; // let graphview generate
														// Y-axis label for
														// us
									}
								});

						// scroll and scale on
						tGraphView.setViewPort(0, tCompleteData.getTimeLabels()
								.size() - 1); // full
												// viewPort
						tGraphView.setScrollable(true);
						tGraphView.setScalable(true);
					}
				}.execute();
				if (BuildConfig.DEBUG)
					Log.w("display", "end time graph");

				break;
			case GraphActivity.TIME_AND_TYPE_GRAPH:
				if (BuildConfig.DEBUG)
					Log.w("display", "begin time and type graph");
				new Object() {
					void execute() {
						// init graphview
						tGraphView = new LineGraphView(tContext // context
								, "Evolution in time per type"// heading
						);
						// init graphViewData
						ArrayList<ArrayList<GraphViewData>> graphViewDataList = new ArrayList<ArrayList<GraphViewData>>();

						// number of types
						int numberOfTypes = tCompleteData.getNumberOfTypes();
						// get amount TODO : getAmoutPerType
						double amount = tAccount.getAmountUpTo(
								tContext.getContentResolver(),
								tCompleteData.getLowerDate());
						// an amount for each type
						double amounts[] = new double[numberOfTypes];
						// an array for each type
						for (int i = 0; i < numberOfTypes; i++) {
							graphViewDataList
									.add(new ArrayList<GraphViewData>());
							amounts[i] = amount; // initialize each amount
						}

						// construct data ----------------
						ArrayList<ArrayList<CompleteDataPoint>> dataArray = tCompleteData
								.getData();
						int dataArraySize = dataArray.size();
						// convert the complete data in graph data
						for (int j = 0; j < dataArraySize; j++) {
							ArrayList<CompleteDataPoint> column = dataArray
									.get(j);
							for (int i = 0; i < numberOfTypes; i++) {
								CompleteDataPoint cell = column.get(i);
								amounts[i] += cell.getValue();
								graphViewDataList.get(i).add(
										new GraphViewData(j, amounts[i]));
							}
						}

						ArrayList<GraphViewSeries> dataSeriesList = new ArrayList<GraphViewSeries>();
						// for each serie
						for (int i = 0; i < numberOfTypes; i++) {
							ArrayList<GraphViewData> graphViewDataForType = graphViewDataList
									.get(i);

							// convert data in data series
							GraphViewData[] graphViewDataArray = graphViewDataForType
									.toArray(new GraphViewData[graphViewDataForType
											.size()]);

							// add serie to list
							GraphViewSeries dataSeries = new GraphViewSeries(
									tCompleteData.getTypeLabels().get(i),
									new GraphViewSeries.GraphViewSeriesStyle(
											tCompleteData.getTypesColors().get(
													i), 3), graphViewDataArray);
							dataSeriesList.add(dataSeries);
						}

						// add series list to graph
						for (GraphViewSeries series : dataSeriesList)
							tGraphView.addSeries(series);

						// date labels
						tGraphView
								.setCustomLabelFormatter(new CustomLabelFormatter() {
									@Override
									public String formatLabel(double value,
											boolean isValueX) {
										if (isValueX) {
											// return the date string
											return Filter
													.millisToText(tCompleteData
															.getTimeLabels()
															.get((int) value));
										}
										return null; // let graphview generate
														// Y-axis label for
														// us
									}
								});

						// scroll and scale on
						tGraphView.setViewPort(0, tCompleteData.getTimeLabels()
								.size() - 1); // full
												// viewPort
						tGraphView.setScrollable(true);
						tGraphView.setScalable(true);
						// set legend
						tGraphView.setShowLegend(true);
						tGraphView.setLegendAlign(LegendAlign.BOTTOM);
						tGraphView.setLegendWidth(200);
					}
				}.execute();
				if (BuildConfig.DEBUG)
					Log.w("display", "end time and type graph");
				break;
			}

			// style ------
			GraphViewStyle style = tGraphView.getGraphViewStyle();

			style.setVerticalLabelsColor(tContext.getResources().getColor(
					R.color.black));
			style.setHorizontalLabelsColor(tContext.getResources().getColor(
					R.color.black));
			style.setTextSize(tContext.getResources().getDimension(
					R.dimen.graph_textsize));
			// end style --

			if (BuildConfig.DEBUG)
				Log.w("display", "end");
		}

		@Override
		protected void onPostExecute(Void result) {
			progressDialog.dismiss();
			tContext.afterLoadAndDisplayTask(tGraphView, tCompleteData);
		}

	}

	// open the filter activity
	private void setFilter() {
		Intent intent = new Intent(this, FilterActivity.class);
		intent.putExtra(Filter.class.toString(), mFilter);
		startActivityForResult(intent, FILTER_ACTIVITY);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case FILTER_ACTIVITY:
			if (resultCode == Activity.RESULT_OK) {
				mFilter = (Filter) data.getSerializableExtra(Filter.class
						.toString());
				this.loadAndDisplay(true, true);
			}
		}
	}

	// ADD and TYPES BUTTON IN ACTION BAR ------------------------

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_graph, menu);

		// GRAPH TYPE SPINNER

		// source :
		// http://android-ed.blogspot.ch/2013/04/adding-spinner-to-actionbarsherlock.html
		// (12.05.14)
		SpinnerAdapter mSpinnerAdapter;
		// if (Build.VERSION.SDK_INT <= 10) {
		mSpinnerAdapter = ArrayAdapter.createFromResource(this,
				R.array.graph_types, R.layout.simple_spinner_item_dark);// android.R.layout.simple_spinner_item);
		// } else {
		// mSpinnerAdapter = ArrayAdapter.createFromResource(this,
		// R.array.graph_types,
		// android.R.layout.simple_spinner_dropdown_item);
		// }
		OnNavigationListener mOnNavigationListener = new OnNavigationListener() {
			// Get the same strings provided for the drop-down's ArrayAdapter
			// String[] strings =
			// getResources().getStringArray(R.array.nav_list);

			@Override
			public boolean onNavigationItemSelected(int position, long itemId) {
				Log.w("actionBar listener", "position " + position);
				switch (position) {
				case 0:
					Log.w("actionBar listener", "time graph");
					mGraphType = TIME_GRAPH;
					break;
				case 1:
					Log.w("actionBar listener", "type graph");
					mGraphType = TYPE_GRAPH;
					break;
				case 2:
					mGraphType = TIME_AND_TYPE_GRAPH;
					break;
				}

				// to avoid the first "autoselection" when the activity is
				// created
				if (!firstFill)
					loadAndDisplay(false, true);

				return true;
			}
		};

		ActionBar actionBar = getSupportActionBar();
		actionBar.setListNavigationCallbacks(mSpinnerAdapter,
				mOnNavigationListener);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_graph_filter:
			setFilter();
			return true;
		case R.id.menu_graph_help:
			if (mTooltipGeneral == null && this.mTooltipTypes == null)
				nextTooltip(null);
			return true;
		case android.R.id.home:
			finish();
			return true;

		}

		return super.onOptionsItemSelected(item);
	}

	private void makeToast(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

	// TOOLTIPS -------------------

	// display the first or the next tooltip
	private void nextTooltip(ToolTipView toolTipView) {
		if (toolTipView == null) {
			addGeneralTooltip();
		} else if (toolTipView == mTooltipGeneral) {
			mTooltipGeneral = null;
			addTypesTooltip();
		} else if (toolTipView == mTooltipTypes) {
			mTooltipTypes = null;
		}
	}

	private void addGeneralTooltip() {
		mTooltipGeneral = mTooltipLayout
				.showToolTipForView(
						new ToolTip()
								.withText(
										"Here you can display a graph for your\naccount. But only the transactions\nselected by the filter are taken in\naccount.")
								.withColor(
										getResources().getColor(
												R.color.holo_orange))
								.withShadow(true),
						findViewById(R.id.menu_graph_help));
		mTooltipGeneral.setOnToolTipViewClickedListener(this);
	}

	private void addTypesTooltip() {
		mTooltipTypes = mTooltipLayout
				.showToolTipForView(
						new ToolTip()
								.withText(
										"Set the the type of graph you want :\n"
												+ "   - TIME : date on x-axis and amount on y-axis\n"
												+ "   - TYPE : type on x-axis and amount on y-axis\n"
												+ "   - BOTH : same as TIME, but with a line per type")
								.withColor(
										getResources().getColor(
												R.color.holo_orange))
								.withShadow(true),
						findViewById(R.id.menu_graph_help));// menu_graph_spinner));
		mTooltipTypes.setOnToolTipViewClickedListener(this);
	}

	@Override
	public void onToolTipViewClicked(ToolTipView toolTipView) {
		nextTooltip(toolTipView);
	}

	// DATA CLASSES

	/**
	 * a simple data class that contains x and y values --> only for the graph
	 * display
	 */
	static private class GraphViewData implements GraphViewDataInterface {
		private int mX;
		private double mY;

		public GraphViewData(int x, double y) {
			mX = x;
			mY = y;
		}

		@Override
		public double getX() {
			return mX;
		}

		@Override
		public double getY() {
			return mY;
		}
	}

	/**
	 * class which stores the data points in this form :
	 * 
	 * type \ time | day1 | day2 | day3 | .................................
	 * ------------|------|------|------|-----------------------------------
	 * type 1 _____|[data]|[data]|[data]| .................................
	 * ------------|------|------|------|-----------------------------------
	 * type 2 _____|[data]|[data]|[data]| .................................
	 * ------------|------|------|------|-----------------------------------
	 * ..............
	 * 
	 * And each [data] is a CompleteDataPoint
	 */
	static private class CompleteData {
		private ArrayList<ArrayList<CompleteDataPoint>> mData;
		ArrayList<Long> mTimeLabels; // time stamps
		ArrayList<String> mTypesLabels;
		private ArrayList<Integer> mTypesColors;
		ArrayList<Long> mTypesId;

		public CompleteData(ArrayList<Type> types) {
			setData(new ArrayList<ArrayList<CompleteDataPoint>>());
			mTimeLabels = new ArrayList<Long>();
			mTypesLabels = new ArrayList<String>();
			mTypesId = new ArrayList<Long>();
			mTypesColors = new ArrayList<Integer>();

			// set types
			for (Type t : types) {
				mTypesLabels.add(t.getName());
				mTypesId.add(t.getId());
				getTypesColors().add(t.getColor());
			}
		}

		public ArrayList<String> getTypeLabels() {
			return mTypesLabels;
		}

		public long getLowerDate() {
			return mTimeLabels.get(0);
		}

		public ArrayList<Long> getTimeLabels() {
			return mTimeLabels;
		}

		public int getNumberOfTypes() {
			return mTypesId.size();
		}

		public void addPoint(long time, long typeId, double value) {
			Log.w("completeData", "add point time = " + time + " typeId = "
					+ typeId + " value = " + value);

			int timeIndex = mTimeLabels.indexOf(time);
			// if timestamp doesn't match, try to see if the day already exists
			if (timeIndex == -1 && mTimeLabels.size() > 0) {
				int lastTimeInd = mTimeLabels.size() - 1;
				String lastLabel = Filter.millisToText(mTimeLabels
						.get(lastTimeInd));
				String newLabel = Filter.millisToText(time);
				if (lastLabel.equals(newLabel)) // if same day
					timeIndex = lastTimeInd;
			}
			int typeIndex = mTypesId.indexOf(typeId);
			if (timeIndex != -1) { // if timestamp already exists, simply add
									// the amount
				getData().get(timeIndex).get(typeIndex).addValue(value);
			} else { // if timestamp does not exist, add it
				Log.w("completeData", "add column " + time);
				ArrayList<CompleteDataPoint> column = new ArrayList<CompleteDataPoint>();
				// create cell for each type
				for (int i = 0; i < mTypesId.size(); i++) {
					if (i == typeIndex) // directly set the point to the value
						column.add(new CompleteDataPoint(value));
					else
						column.add(new CompleteDataPoint(0));

				}
				// add time column
				mTimeLabels.add(time);
				getData().add(column);
			}
		}

		/**
		 * add a transaction to the data <br/>
		 * !!! transactions must be passed in "chronologic" order ( -> growing
		 * date)
		 */
		public void addTransaction(Transaction trans) {
			addPoint(trans.getDate(), trans.getType().getId(),
					trans.getAmount());
		}

		public ArrayList<ArrayList<CompleteDataPoint>> getData() {
			return mData;
		}

		public void setData(ArrayList<ArrayList<CompleteDataPoint>> mData) {
			this.mData = mData;
		}

		public ArrayList<Integer> getTypesColors() {
			return mTypesColors;
		}
	}

	/**
	 * class to store one data point
	 */
	static private class CompleteDataPoint {
		// private long mTime;
		// private int mType;
		private double mValue;

		// public long getTime() {
		// return mTime;
		// }

		public CompleteDataPoint(double initialValue) {
			mValue = initialValue;
		}

		public void addValue(double value) {
			mValue += value;
		}

		//
		// public void setTime(long mTime) {
		// this.mTime = mTime;
		// }
		//
		// public int getType() {
		// return mType;
		// }
		//
		// public void setType(int mType) {
		// this.mType = mType;
		// }

		public double getValue() {
			return mValue;
		}

		public void setValue(double mValue) {
			this.mValue = mValue;
		}

	}

}
