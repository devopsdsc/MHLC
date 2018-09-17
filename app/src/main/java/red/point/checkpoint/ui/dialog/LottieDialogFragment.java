package red.point.checkpoint.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import com.airbnb.lottie.Cancellable;
import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.facebook.shimmer.ShimmerFrameLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import red.point.checkpoint.R;

public class LottieDialogFragment extends DialogFragment {

    private static final String TAG = LottieDialogFragment.class.getSimpleName();

    @BindView(R.id.description)
    TextView mDescription;

    @BindView(R.id.animation_view)
    LottieAnimationView animationView;

    @BindView(R.id.shimmer_view_container)
    ShimmerFrameLayout shimmerFrameLayout;

    private Unbinder unbinder;

    private AlertDialog.Builder builder;

    private Cancellable compositionLoader;

    private View rootView;

    public LottieDialogFragment() {}

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        rootView = getActivity().getLayoutInflater().inflate(R.layout.fragment_dialog_lottie, null);

        unbinder = ButterKnife.bind(this, rootView);

        mDescription.setVisibility(View.GONE);

        compositionLoader = LottieComposition.Factory.fromRawFile(getContext(), R.raw.gift, (composition) -> {
            animationView.setComposition(composition);
            animationView.playAnimation();
            animationView.setRepeatMode(LottieDrawable.INFINITE);

            if (animationView.isAnimating()) {
                shimmerFrameLayout.stopShimmerAnimation();
                shimmerFrameLayout.setVisibility(View.GONE);
                mDescription.setVisibility(View.VISIBLE);
                animationView.setOnClickListener(v -> dismiss());
            }
        });

        return alertDialog();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (unbinder != null) unbinder.unbind();

        if (compositionLoader != null) compositionLoader.cancel();
    }

    private AlertDialog alertDialog() {
        builder = new AlertDialog.Builder(getActivity());
        builder.setView(rootView);

        return builder.create();
    }
}
