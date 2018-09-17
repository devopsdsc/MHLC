package red.point.checkpoint.helper;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import red.point.checkpoint.R;

public class FragmentHelper {

    public static void replace(FragmentActivity context, Fragment newFragment, @Nullable Bundle bundle, boolean hasBackStack) {
        FragmentManager fragmentManager = context.getSupportFragmentManager();

        Fragment currentFragment = context.getSupportFragmentManager().findFragmentById(R.id.mainContent);

        if (bundle != null) newFragment.setArguments(bundle);

        FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.setCustomAnimations(R.anim.slide_in, R.anim.slide_out, R.anim.back_in, R.anim.back_out);
        transaction.replace(R.id.mainContent, newFragment);

        // Add every fragment to the back stack
        // This behaviour needed because every fragment need to be added
        // To the back stack to prevent overlapping when back button pressed
        if (hasBackStack) {
            transaction.addToBackStack(currentFragment.getClass().getSimpleName());
        } else {
            transaction.addToBackStack(null);
        }

        transaction.commit();
    }

    public static void popBackStack(FragmentActivity context, String tag) {
        FragmentManager fragmentManager = context.getSupportFragmentManager();

        fragmentManager.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    public static void show(FragmentActivity context, DialogFragment dialogFragment, Bundle bundle) {
        FragmentManager fragmentManager = context.getSupportFragmentManager();

        if (bundle != null) dialogFragment.setArguments(bundle);

        dialogFragment.show(fragmentManager, dialogFragment.getClass().getSimpleName());
    }
}
