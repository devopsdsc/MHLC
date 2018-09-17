package red.point.checkpoint.ui.reward;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import red.point.checkpoint.R;

/**
 * Reward Fragment
 */
public class RewardFragment extends Fragment {

    private Unbinder unbinder;

    public RewardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_reward, container, false);
        // Binding view
        unbinder = ButterKnife.bind(rootView);

        if (getActivity() != null) getActivity().setTitle(getString(R.string.nav_reward));

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null) unbinder.unbind();
    }
}
