package yu.heetae.android.tilt;

import android.support.v4.app.Fragment;

public class TiltActivity extends SingleFragmentActivity{

    @Override
    protected Fragment createFragment() {
        return new SettingsFragment();
    }

}
