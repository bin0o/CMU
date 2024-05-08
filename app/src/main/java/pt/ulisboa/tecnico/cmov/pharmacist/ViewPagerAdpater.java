package pt.ulisboa.tecnico.cmov.pharmacist;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdpater extends FragmentStateAdapter {
    public ViewPagerAdpater(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new PickOnMapTabFragment();
            case 1: return new CurrentLocationTabFragment();
            case 2: return new ManualAddressTabFragment();
            default: return new PickOnMapTabFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
