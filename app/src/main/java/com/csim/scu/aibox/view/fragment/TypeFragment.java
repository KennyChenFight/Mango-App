package com.csim.scu.aibox.view.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.csim.scu.aibox.R;
import com.csim.scu.aibox.callback.TypeCallback;
import com.csim.scu.aibox.log.Logger;
import com.csim.scu.aibox.view.activity.MainActivity;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TypeFragment extends Fragment implements TypeCallback {

    private final int PLACE_PICKER = 0;
    private FragmentManager fragmentManager;
    private ImageButton ibBack;
    private ImageButton ibBus;
    private ImageButton ibFood;
    private ImageButton ibHospital;
    private ImageButton ibHotel;
    private ImageButton ibPark;
    private ImageButton ibParking;
    private ImageButton ibPostoffice;
    private ImageButton ibConvenience_store;
    private Button btPlacePicker;
    private Button btAr;
    private HashMap<String, String> place_location = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_type, container, false);
        fragmentManager = getFragmentManager();
        hideBar();
        findViews(view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }


    private void hideBar() {
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Logger.d();
    }

    private void findViews(View view) {
        ibBack = view.findViewById(R.id.ibBack);
        ibBus = view.findViewById(R.id.ibBus);
        ibFood = view.findViewById(R.id.ibFood);
        ibHospital = view.findViewById(R.id.ibHospital);
        ibHotel = view.findViewById(R.id.ibHotel);
        ibPark = view.findViewById(R.id.ibPark);
        ibParking = view.findViewById(R.id.ibParking);
        ibPostoffice = view.findViewById(R.id.ibPostOffice);
        ibConvenience_store = view.findViewById(R.id.ibStore);
        btPlacePicker = view.findViewById(R.id.btPlacePicker);
        btAr = view.findViewById(R.id.btAr);
        setImageButtonListener();
        setTvCancelListener();
        setButtonListener();
    }


    private void setImageButtonListener() {
        // todo
        ibBus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                NavigationFragment navigationFragment = new NavigationFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("type", R.drawable.navigation_bus);
                bundle.putBoolean("define", true);
                navigationFragment.setArguments(bundle);
                fragmentManager.executePendingTransactions();
                fragmentTransaction.replace(R.id.container, navigationFragment, navigationFragment.getClass().getName()).commit();
            }
        });

        ibFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                NavigationFragment navigationFragment = new NavigationFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("type", R.drawable.navigation_food);
                bundle.putBoolean("define", true);
                navigationFragment.setArguments(bundle);
                fragmentManager.executePendingTransactions();
                fragmentTransaction.replace(R.id.container, navigationFragment, navigationFragment.getClass().getName()).commit();
            }
        });

        ibHospital.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                NavigationFragment navigationFragment = new NavigationFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("type", R.drawable.navigation_hospital);
                bundle.putBoolean("define", true);
                navigationFragment.setArguments(bundle);
                fragmentManager.executePendingTransactions();
                fragmentTransaction.replace(R.id.container, navigationFragment, navigationFragment.getClass().getName()).commit();
            }
        });

        ibHotel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                NavigationFragment navigationFragment = new NavigationFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("type", R.drawable.navigation_hotel);
                bundle.putBoolean("define", true);
                navigationFragment.setArguments(bundle);
                fragmentManager.executePendingTransactions();
                fragmentTransaction.replace(R.id.container, navigationFragment, navigationFragment.getClass().getName()).commit();
            }
        });

        ibPark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                NavigationFragment navigationFragment = new NavigationFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("type", R.drawable.navigation_park);
                bundle.putBoolean("define", true);
                navigationFragment.setArguments(bundle);
                fragmentManager.executePendingTransactions();
                fragmentTransaction.replace(R.id.container, navigationFragment, navigationFragment.getClass().getName()).commit();
            }
        });

        ibParking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                NavigationFragment navigationFragment = new NavigationFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("type", R.drawable.navigation_parking);
                bundle.putBoolean("define", true);
                navigationFragment.setArguments(bundle);
                fragmentManager.executePendingTransactions();
                fragmentTransaction.replace(R.id.container, navigationFragment, navigationFragment.getClass().getName()).commit();
            }
        });

        ibPostoffice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                NavigationFragment navigationFragment = new NavigationFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("type", R.drawable.navigation_postoffice);
                bundle.putBoolean("define", true);
                navigationFragment.setArguments(bundle);
                fragmentManager.executePendingTransactions();
                fragmentTransaction.replace(R.id.container, navigationFragment, navigationFragment.getClass().getName()).commit();
            }
        });

        ibConvenience_store.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                NavigationFragment navigationFragment = new NavigationFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("type", R.drawable.navigation_store);
                bundle.putBoolean("define", true);
                navigationFragment.setArguments(bundle);
                fragmentManager.executePendingTransactions();
                fragmentTransaction.replace(R.id.container, navigationFragment, navigationFragment.getClass().getName()).commit();
            }
        });

    }

    private void setTvCancelListener() {
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentManager.popBackStackImmediate();
                fragmentManager.popBackStackImmediate();
                List<Fragment> fragments =  fragmentManager.getFragments();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                for (Fragment fragment : fragments) {
                    fragmentTransaction.hide(fragment);
                }
                fragmentManager.executePendingTransactions();
                fragmentTransaction.commit();
            }
        });
    }

    private void setButtonListener() {
        btPlacePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                    startActivityForResult(builder.build(getActivity()), PLACE_PICKER);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e){
                    Logger.d(e.toString());
                }
            }
        });
        btAr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                NavigationFragment navigationFragment = new NavigationFragment();
                Bundle bundle = new Bundle();
                bundle.putSerializable("place_location", place_location);
                bundle.putBoolean("which", false);
                navigationFragment.setArguments(bundle);
                fragmentManager.executePendingTransactions();
                fragmentTransaction.replace(R.id.container, navigationFragment, navigationFragment.getClass().getName()).commit();
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PLACE_PICKER) {
            Place place = PlacePicker.getPlace(getActivity(), data);
            place_location.put("lat", String.valueOf(place.getLatLng().latitude));
            place_location.put("lng", String.valueOf(place.getLatLng().longitude));
        }
    }

    @Override
    public void backToMain() {
        List<Fragment> fragments =  fragmentManager.getFragments();
        fragmentManager.popBackStackImmediate();
        fragmentManager.popBackStackImmediate();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        for (Fragment fragment : fragments) {
            fragmentTransaction.hide(fragment);
        }
        fragmentManager.executePendingTransactions();
        fragmentTransaction.commit();
    }
}
