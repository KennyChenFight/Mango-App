package com.csim.scu.aibox.view.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;

import com.csim.scu.aibox.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by kenny on 2018/7/20.
 */

public class UserHabitFragment extends Fragment {

    private FragmentManager fragmentManager;
    private CheckBox cbSmoking_yes;
    private CheckBox cbSmoking_no;
    private CheckBox cbExercise_yes;
    private CheckBox cbExercise_no;
    private ImageButton ibBack;
    private Map<String, String> map;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_userhabit, container, false);
        fragmentManager = getFragmentManager();
        map = new HashMap<>();
        map.put("smoking", this.getArguments().getString("smoking"));
        map.put("exercise", this.getArguments().getString("exercise"));
        findViews(view);
        return view;
    }

    private void findViews(View view) {
        cbSmoking_yes = view.findViewById(R.id.cbSmoking_yes);
        cbSmoking_no = view.findViewById(R.id.cbSmoking_no);
        cbExercise_yes = view.findViewById(R.id.cbExercise_yes);
        cbExercise_no = view.findViewById(R.id.cbExercise_no);
        ibBack = view.findViewById(R.id.ibBack);
        setHabitItem();
        setBackListener();
    }

    private void setHabitItem() {
        cbSmoking_yes.setClickable(false);
        cbSmoking_no.setClickable(false);
        cbExercise_yes.setClickable(false);
        cbExercise_no.setClickable(false);
        if (map.get("smoking").equals("True")) {
            cbSmoking_yes.setChecked(true);
        }
        else {
            cbSmoking_no.setChecked(true);
        }
        if (map.get("exercise").equals("True")) {
            cbExercise_yes.setChecked(true);
        }
        else {
            cbExercise_no.setChecked(true);
        }
    }

    private void setBackListener() {
        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragmentManager.popBackStackImmediate();
            }
        });
    }
}

