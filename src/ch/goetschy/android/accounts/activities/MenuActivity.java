package ch.goetschy.android.accounts.activities;

import com.actionbarsherlock.app.SherlockActivity;
import com.haarman.supertooltips.ToolTip;
import com.haarman.supertooltips.ToolTipRelativeLayout;
import com.haarman.supertooltips.ToolTipView;

import ch.goetschy.android.accounts.R;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

public class MenuActivity extends SherlockActivity implements
		ToolTipView.OnToolTipViewClickedListener {

	private ToolTipView mTooltipAccounts = null;
	private ToolTipView mTooltipTypes = null;
	private ToolTipView mTooltipDetect = null;
	private ToolTipRelativeLayout mTooltipLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_menu);

		mTooltipLayout = (ToolTipRelativeLayout) findViewById(R.id.activity_menu_tooltiplayout);

		Button overviewBut = (Button) findViewById(R.id.activity_menu_button_overview);
		Button typesBut = (Button) findViewById(R.id.activity_menu_button_types);
		Button saveBut = (Button) findViewById(R.id.activity_menu_button_save);
		Button coinBut = (Button) findViewById(R.id.activity_menu_button_coin_detect);
		Button quitBut = (Button) findViewById(R.id.activity_menu_button_quit);

		Button tooltipsBut = (Button) findViewById(R.id.activity_menu_button_tooltips);

		// LISTENERS -----------------------

		// overview
		overviewBut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MenuActivity.this,
						AccountsOverviewActivity.class);
				startActivity(intent);
			}
		});

		// type manager
		typesBut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MenuActivity.this,
						ManageTypesActivity.class);
				startActivity(intent);
			}
		});

		// save - restore activity
		saveBut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MenuActivity.this,
						SaveRestoreActivity.class);
				startActivity(intent);
			}
		});

		// coin recognition activity
		coinBut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MenuActivity.this,
						CoinDetectionActivity.class);
				startActivity(intent);
			}
		});

		// quit button
		quitBut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		// tooltips button
		tooltipsBut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mTooltipTypes == null && mTooltipAccounts == null && mTooltipDetect == null)
					nextTooltip(null);
			}
		});

		// ----------------------------------
	}

	// TOOLTIPS -------------------

	// display the first or the next tooltip
	private void nextTooltip(ToolTipView toolTipView) {
		if (toolTipView == null) {
			addTypesTooltip();
		} else if (toolTipView == mTooltipTypes) {
			mTooltipTypes = null;
			addAccountsTooltip();
		} else if (toolTipView == mTooltipAccounts) {
			mTooltipAccounts = null; 
			addDetectTooltip();
		} else if (toolTipView == mTooltipDetect) {
			mTooltipDetect = null;
		}
	}

	private void addAccountsTooltip() {
		mTooltipAccounts = mTooltipLayout.showToolTipForView(new ToolTip()
				.withText("Display your accounts and\nadd new transactions.")
				.withColor(getResources().getColor(R.color.holo_orange))
				.withShadow(true),
				findViewById(R.id.activity_menu_button_overview));
		mTooltipAccounts.setOnToolTipViewClickedListener(MenuActivity.this);
	}

	private void addDetectTooltip() {
		mTooltipDetect = mTooltipLayout.showToolTipForView(new ToolTip()
				.withText("Scan coins with the\ncamera and count the\namount of money.")
				.withColor(getResources().getColor(R.color.holo_orange))
				.withShadow(true),
				findViewById(R.id.activity_menu_button_coin_detect));
		mTooltipDetect.setOnToolTipViewClickedListener(MenuActivity.this);
	}


	private void addTypesTooltip() {
		mTooltipTypes = mTooltipLayout
				.showToolTipForView(
						new ToolTip()
								.withText(
										"Create and modify your own\ntypes of transactions to manage\nyour accounts.")
								.withColor(
										getResources().getColor(
												R.color.holo_orange))
								.withShadow(true),
						findViewById(R.id.activity_menu_button_types));
		mTooltipTypes.setOnToolTipViewClickedListener(MenuActivity.this);
	}

	@Override
	public void onToolTipViewClicked(ToolTipView toolTipView) {
		nextTooltip(toolTipView);
	}

}
