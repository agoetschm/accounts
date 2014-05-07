package ch.goetschy.android.accounts.activities;

import android.os.Bundle;
import android.widget.LinearLayout;

import ch.goetschy.android.accounts.R;

import com.actionbarsherlock.app.SherlockActivity;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewDataInterface;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewStyle;
import com.jjoe64.graphview.LineGraphView;

public class GraphActivity extends SherlockActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.activity_graph);
		
		// init example series data
		GraphViewSeries exampleSeries = new GraphViewSeries(new GraphViewData[] {
		    new GraphViewData(1, 2.0d)
		    , new GraphViewData(2, 1.5d)
		    , new GraphViewData(3, 2.5d)
		    , new GraphViewData(4, 1.0d)
		});
		 
		GraphView graphView = new LineGraphView(
		    this // context
		    , "GraphViewDemo" // heading
		);
		graphView.addSeries(exampleSeries); // data
		
		// style ------
		GraphViewStyle style = graphView.getGraphViewStyle();
		
		style.setVerticalLabelsColor(getResources().getColor(R.color.black));
		style.setHorizontalLabelsColor(getResources().getColor(R.color.black));
		
		// end style --
		 
		LinearLayout layout = (LinearLayout) findViewById(R.id.activity_graph_layout);
		layout.addView(graphView);
	}
	
	
	
	
	/*
	 * a simple data class that contains x and y values
	 */
	private class GraphViewData implements GraphViewDataInterface {
		private int mX;
		private double mY;
		public GraphViewData(int x, double y){
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
	
}
