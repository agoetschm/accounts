package ch.goetschy.android.accounts.activities;

import ch.goetschy.android.accounts.R;
import ch.goetschy.android.accounts.R.layout;
import ch.goetschy.android.accounts.R.menu;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;

public class AccountsOverviewActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        Log.w("overviewActivity", "test");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_overview, menu);
        return true;
    }
}
