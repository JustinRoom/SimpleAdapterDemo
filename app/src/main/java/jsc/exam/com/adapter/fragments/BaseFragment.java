package jsc.exam.com.adapter.fragments;

import android.content.Context;
import android.support.v4.app.Fragment;

public abstract class BaseFragment extends Fragment {

    private boolean isDataLoaded = false;

    abstract void onLoadData(Context context);

    @Override
    public void onResume() {
        super.onResume();
        if (!isDataLoaded) {
            isDataLoaded = true;
            if (getActivity() != null)
                onLoadData(getActivity());
        }
    }
}
