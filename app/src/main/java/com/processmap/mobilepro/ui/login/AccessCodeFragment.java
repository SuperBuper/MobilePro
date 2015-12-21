package com.processmap.mobilepro.ui.login;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.processmap.mobilepro.R;


public class AccessCodeFragment extends Fragment {

    View fragment;

    TextView[] mPins;
    Button[] mButtons;

    int numbersCount;

    OnAccessCodeFragmentInteractionListener mListener;

    public AccessCodeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragment = inflater.inflate(R.layout.fragment_access_code, container, false);

        Typeface iconFont = Typeface.createFromAsset(getActivity().getAssets(), "fonts/MaterialIcons-Regular.ttf");

        mPins = new TextView[6];

        mPins[0] = (TextView) fragment.findViewById(R.id.editPin1);
        mPins[1] = (TextView) fragment.findViewById(R.id.editPin2);
        mPins[2] = (TextView) fragment.findViewById(R.id.editPin3);
        mPins[3] = (TextView) fragment.findViewById(R.id.editPin4);
        mPins[4] = (TextView) fragment.findViewById(R.id.editPin5);
        mPins[5] = (TextView) fragment.findViewById(R.id.editPin6);

        mButtons = new Button[12];

        mButtons[1] = (Button) fragment.findViewById(R.id.num_1);
        mButtons[2] = (Button) fragment.findViewById(R.id.num_2);
        mButtons[3] = (Button) fragment.findViewById(R.id.num_3);
        mButtons[4] = (Button) fragment.findViewById(R.id.num_4);
        mButtons[5] = (Button) fragment.findViewById(R.id.num_5);
        mButtons[6] = (Button) fragment.findViewById(R.id.num_6);
        mButtons[7] = (Button) fragment.findViewById(R.id.num_7);
        mButtons[8] = (Button) fragment.findViewById(R.id.num_8);
        mButtons[9] = (Button) fragment.findViewById(R.id.num_9);
        mButtons[0] = (Button) fragment.findViewById(R.id.num_0);
        mButtons[10] = (Button) fragment.findViewById(R.id.button_cancel);
        mButtons[11] = (Button) fragment.findViewById(R.id.button_ok);

        clearPin();

        mButtons[10].setTypeface(iconFont);
        mButtons[10].setText("\uE14A");
        mButtons[11].setTypeface(iconFont);
        mButtons[11].setText("\uE5CA");

        mButtons[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNumberClick("1");
            }
        });
        mButtons[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNumberClick("2");
            }
        });
        mButtons[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNumberClick("3");
            }
        });
        mButtons[4].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNumberClick("4");
            }
        });
        mButtons[5].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNumberClick("5");
            }
        });
        mButtons[6].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNumberClick("6");
            }
        });
        mButtons[7].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNumberClick("7");
            }
        });
        mButtons[8].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNumberClick("8");
            }
        });
        mButtons[9].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNumberClick("9");
            }
        });
        mButtons[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNumberClick("0");
            }
        });
        mButtons[10].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (numbersCount > 0) {
                    vibrate();
                    numbersCount--;
                    mPins[numbersCount].setText("_");
                    mButtons[11].setVisibility(View.INVISIBLE);
                }
            }
        });
        mButtons[11].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vibrate();
                onOK();
            }
        });

        clearPin();
        enableControls(true);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnAccessCodeFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnAccessCodeFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void onNumberClick(String number) {
        if (numbersCount < 6) {
            vibrate();
            mPins[numbersCount].setText(number);
            numbersCount++;
            mButtons[11].setVisibility(numbersCount == 6 ? View.VISIBLE : View.INVISIBLE);
        }
    }

    public void clearPin() {
        for (int i = 0; i < 6; i++) {
            mPins[i].setText("_");
        }
        numbersCount = 0;
        mButtons[11].setVisibility(View.INVISIBLE);
    }

    private void onOK() {
        if (mListener != null) {
            enableControls(false);
            mListener.onAccessCode(this, String.format("%s%s%s%s%s%s", mPins[0].getText(),
                    mPins[1].getText(),
                    mPins[2].getText(),
                    mPins[3].getText(),
                    mPins[4].getText(),
                    mPins[5].getText()));
        }
    }

    public void enableControls(Boolean enable) {
        for (int i = 0; i < 12; i++) {
            mButtons[i].setEnabled(enable);
        }
    }

    public interface OnAccessCodeFragmentInteractionListener {
        public void onAccessCode(AccessCodeFragment fragment, String pin);
    }

    private void vibrate() {
        Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(100);
    }
}