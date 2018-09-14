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

public class UserDiseaseFragment extends Fragment{

    private FragmentManager fragmentManager;
    private Map<String, String> map;
    private CheckBox cbHeart_problem_yes;
    private CheckBox cbHeart_problem_no;
    private CheckBox cbStroke_yes;
    private CheckBox cbStroke_no;
    private CheckBox cbHigh_blood_yes;
    private CheckBox cbHigh_blood_no;
    private CheckBox cbHigh_cholesterol_yes;
    private CheckBox cbHigh_cholesterol_no;
    private CheckBox cbDiabetes_yes;
    private CheckBox cbDiabetes_no;
    private ImageButton ibBack;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_userdisease, container, false);
        fragmentManager = getFragmentManager();
        map = new HashMap<>();
        map.put("heart_problem", this.getArguments().getString("heart_problem"));
        map.put("stroke", this.getArguments().getString("stroke"));
        map.put("high_blood", this.getArguments().getString("high_blood"));
        map.put("high_cholesterol", this.getArguments().getString("high_cholesterol"));
        map.put("diabetes", this.getArguments().getString("diabetes"));
        findViews(view);
        return view;
    }

    private void findViews(View view) {
        cbHeart_problem_yes = view.findViewById(R.id.cbHeart_problem_yes);
        cbHeart_problem_no = view.findViewById(R.id.cbHeart_problem_no);
        cbStroke_yes = view.findViewById(R.id.cbStroke_yes);
        cbStroke_no = view.findViewById(R.id.cbStroke_no);
        cbHigh_blood_yes = view.findViewById(R.id.cbHigh_blood_yes);
        cbHigh_blood_no = view.findViewById(R.id.cbHigh_blood_no);
        cbHigh_cholesterol_yes = view.findViewById(R.id.cbHigh_cholesterol_yes);
        cbHigh_cholesterol_no = view.findViewById(R.id.cbHigh_cholesterol_no);
        cbDiabetes_yes = view.findViewById(R.id.cbDiabetes_yes);
        cbDiabetes_no = view.findViewById(R.id.cbDiabetes_no);
        ibBack = view.findViewById(R.id.ibBack);
        setDiseaseInfo();
        setBackListener();
    }

    private void setDiseaseInfo() {
        cbHeart_problem_yes.setClickable(false);
        cbHeart_problem_no.setClickable(false);
        cbHigh_blood_yes.setClickable(false);
        cbStroke_yes.setClickable(false);
        cbStroke_no.setClickable(false);
        cbHigh_blood_yes.setClickable(false);
        cbHigh_blood_no.setClickable(false);
        cbHigh_cholesterol_yes.setClickable(false);
        cbHigh_cholesterol_no.setClickable(false);
        cbDiabetes_yes.setClickable(false);
        cbDiabetes_no.setChecked(false);
        if (map.get("heart_problem").equals("True")) {
            cbHeart_problem_yes.setChecked(true);
        }
        else {
            cbHeart_problem_no.setChecked(true);
        }
        if (map.get("stroke").equals("True")) {
            cbStroke_yes.setChecked(true);
        }
        else {
            cbStroke_no.setChecked(true);
        }
        if (map.get("high_blood").equals("True")) {
            cbHigh_blood_yes.setChecked(true);
        }
        else {
            cbHigh_blood_no.setChecked(true);
        }
        if (map.get("high_cholesterol").equals("True")) {
            cbHigh_cholesterol_yes.setChecked(true);
        }
        else {
            cbHigh_cholesterol_no.setChecked(true);
        }
        if (map.get("diabetes").equals("True")) {
            cbDiabetes_yes.setChecked(true);
        }
        else {
            cbDiabetes_no.setChecked(true);
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
