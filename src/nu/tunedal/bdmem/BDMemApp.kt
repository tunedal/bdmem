package nu.tunedal.bdmem

import android.app.Activity
import android.os.Bundle

class BDMemApp : Activity() {
    /** Called when the activity is first created. */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
    }
}
