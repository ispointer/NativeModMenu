//
// Created by aantik on 2/1/2026.
//

package uk.lgl;

import android.app.Activity;
import android.os.Bundle;

import uk.lgl.modmenu.FloatingModMenu;


public class MainActivity extends Activity {

    static {
        System.loadLibrary("nativelgl");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }
}
