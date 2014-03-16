package ch.goetschy.android.accounts.activities;

import ch.goetschy.android.accounts.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MenuActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_menu);
		
		Button overviewBut = (Button) findViewById(R.id.activity_menu_button_overview);
		Button typesBut = (Button) findViewById(R.id.activity_menu_button_types);
		Button saveBut = (Button) findViewById(R.id.activity_menu_button_save);
		Button coinBut = (Button) findViewById(R.id.activity_menu_button_coin_detect);
		Button quitBut = (Button) findViewById(R.id.activity_menu_button_quit);
		
		
		
		// LISTENERS -----------------------
		
		// overview
		overviewBut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MenuActivity.this, AccountsOverviewActivity.class);
				startActivity(intent);
			}
		});
		
		// type manager
		typesBut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MenuActivity.this, ManageTypesActivity.class);
				startActivity(intent);
			}
		});
		
		// save - restore activity
		saveBut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MenuActivity.this, SaveRestoreActivity.class);
				startActivity(intent);
			}
		});
		
		// coin recognition activity
		coinBut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MenuActivity.this, CoinDetectionActivity.class);
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
		
		// ----------------------------------
	}
	
}
