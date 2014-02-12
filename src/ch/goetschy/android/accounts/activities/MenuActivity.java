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
		Button quitBut = (Button) findViewById(R.id.activity_menu_button_quit);
		
		
		// LISTENERS -----------------------
		
		overviewBut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MenuActivity.this, AccountsOverviewActivity.class);
				startActivity(intent);
			}
		});
		
		typesBut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MenuActivity.this, ManageTypesActivity.class);
				startActivity(intent);
			}
		});
		
		saveBut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MenuActivity.this, SaveRestoreActivity.class);
				startActivity(intent);
			}
		});
		
		
		quitBut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		// ----------------------------------
	}
	
}
