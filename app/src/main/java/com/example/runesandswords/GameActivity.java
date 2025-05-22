package com.example.runesandswords;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import android.content.Context;
import android.content.SharedPreferences;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int RED = Color.RED;
    private static final int YELLOW = Color.YELLOW;
    private static final int BLUE = Color.BLUE;
    private static final int GREEN = Color.GREEN;
    private List<ColorButton> buttonList;
    private List<ColorButton> sequence;
    private int sequenceIndex;
    private int score;
    private String weaponName;
    private Button yellowButton;
    private Button redButton;
    private Button greenButton;
    private Button blueButton;
    private Button tomenuButton;
    private TextView scoreTextView;
    private FrameLayout charLayout;
    private LinearLayout endLayout;
    private TextView maxScoreText;
    private ImageView weaponView;
    private SharedPreferences sharedPreferences;
    private final String MAX_SCORE_KEY = "max_score";
    private float initialYPosition;
    private boolean animationInProgress = false;
    private final Random random = new Random();

    private enum ButtonColor {
        RED, YELLOW, BLUE, GREEN
    }

    private static class ColorButton {
        Button button;
        ButtonColor color;

        public ColorButton(Button button, ButtonColor color) {
            this.button = button;
            this.color = color;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences("my_app_prefs", Context.MODE_PRIVATE);
        int currentMaxScore = getMaxScore();

        setContentView(R.layout.activity_game);

        yellowButton = findViewById(R.id.yellowButton);
        redButton = findViewById(R.id.redButton);
        greenButton = findViewById(R.id.greenButton);
        blueButton = findViewById(R.id.blueButton);
        scoreTextView = findViewById(R.id.scoreTextView);
        tomenuButton = findViewById(R.id.tomenuButton);
        scoreTextView = findViewById(R.id.scoreTextView);
        maxScoreText = findViewById(R.id.maxScoreText);
        charLayout = findViewById(R.id.charLayout);
        endLayout = findViewById(R.id.endLayout);
        weaponView = findViewById(R.id.weaponView);

        yellowButton.setOnClickListener(this);
        redButton.setOnClickListener(this);
        greenButton.setOnClickListener(this);
        blueButton.setOnClickListener(this);

        redButton.setBackgroundResource(R.drawable.rblank);
        greenButton.setBackgroundResource(R.drawable.fblank);
        yellowButton.setBackgroundResource(R.drawable.oblank);
        blueButton.setBackgroundResource(R.drawable.tblank);


        tomenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GameActivity.this, MainActivity.class);
                endLayout.setVisibility(View.GONE);
                startActivity(intent);
                finish();
            }
        });

        buttonList = new ArrayList<>();
        buttonList.add(new ColorButton(redButton, ButtonColor.RED));
        buttonList.add(new ColorButton(yellowButton, ButtonColor.YELLOW));
        buttonList.add(new ColorButton(blueButton, ButtonColor.BLUE));
        buttonList.add(new ColorButton(greenButton, ButtonColor.GREEN));

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;

        initialYPosition = -screenHeight;

        weaponView.setTranslationY(initialYPosition);

        sequence = new ArrayList<>();
        sequenceIndex = 0;
        score = 0;

        endLayout.setVisibility(View.GONE);
        charLayout.setVisibility(View.VISIBLE);

        newRound();
    }

    @Override
    public void onClick(View v) {
        Button clickedButton = (Button) v;

        if (clickedButton == yellowButton){
            yellowButton.setBackgroundResource(R.drawable.oyellow);
            yellowButton.postDelayed(new Runnable() {
                @Override
                public void run() {
                    yellowButton.setBackgroundResource(R.drawable.oblank);
                }
            }, 1000);
        }

        else if (clickedButton == redButton){
            redButton.setBackgroundResource(R.drawable.rred);
            redButton.postDelayed(new Runnable() {
                @Override
                public void run() {
                    redButton.setBackgroundResource(R.drawable.rblank);
                }
            }, 1000);
        }

        else if (clickedButton == blueButton){
            blueButton.setBackgroundResource(R.drawable.tblue);
            blueButton.postDelayed(new Runnable() {
                @Override
                public void run() {
                    blueButton.setBackgroundResource(R.drawable.tblank);
                }
            }, 1000);
        }

        else if (clickedButton == greenButton){
            greenButton.setBackgroundResource(R.drawable.fgreen);
            greenButton.postDelayed(new Runnable() {
                @Override
                public void run() {
                    greenButton.setBackgroundResource(R.drawable.fblank);
                }
            }, 1000);
        }

        if (clickedButton == sequence.get(sequenceIndex).button) {
            sequenceIndex++;

            if (sequenceIndex == sequence.size()) {
                disableButtons();
                startSuccessAnimationIn();
                score++;
                updateScore();
            }
        } else {
            startGameOverAnimationIn();
        }
    }

    private void newRound() {
        String[] spriteIds = {
                "sword",
                "axe",
                "dagger"
        };
        int randomIndex = random.nextInt(spriteIds.length);
        weaponName = spriteIds[randomIndex];
        weaponView.setImageResource(getResources().getIdentifier(weaponName, "drawable", getPackageName()));



        weaponView.setAlpha(1f);
        weaponView.setTranslationY(initialYPosition);
        disableButtons();

        startDropAnimation();

        sequence.clear();
        sequenceIndex = 0;

        List<ColorButton> shuffledButtons = new ArrayList<>(buttonList);
        Collections.shuffle(shuffledButtons, new Random());

        sequence.addAll(shuffledButtons);

        StringBuilder sequenceString = new StringBuilder();
        for (ColorButton colorButton : sequence) {
            sequenceString.append(colorButton.color.name()).append(" ");
        }

        //Toast.makeText(this, sequenceString.toString(), Toast.LENGTH_SHORT).show();

        //playSequenceAnimations();
    }

    private void gameOver() {
        updateMaxScore(score);
        int updatedMaxScore = getMaxScore();
        updateScore();

        charLayout.setVisibility(View.GONE);
        endLayout.setVisibility(View.VISIBLE);
        weaponView.setVisibility(View.GONE);

        maxScoreText.setText("Максимальный счёт: " + updatedMaxScore);
    }

    private void updateScore() {
        scoreTextView.setText("Счёт: " + score);
    }

    private void saveMaxScore(int s) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(MAX_SCORE_KEY, s);
        editor.apply();
    }

    private int getMaxScore() { return sharedPreferences.getInt(MAX_SCORE_KEY, 0); }

    private void updateMaxScore(int s) {
        int currentMaxScore = getMaxScore();

        if (s > currentMaxScore) {
            saveMaxScore(s);
        }
    }

    private void startDropAnimation(){
        weaponView.setAlpha(1f);

        if (animationInProgress) return;
        animationInProgress = true;
        ObjectAnimator dropAnimator = ObjectAnimator.ofFloat(weaponView, "translationY", weaponView.getTranslationY(), 0f);
        dropAnimator.setDuration(2000);
        dropAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        dropAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                redButton.setBackgroundResource(R.drawable.rred);
                greenButton.setBackgroundResource(R.drawable.fgreen);
                yellowButton.setBackgroundResource(R.drawable.oyellow);
                blueButton.setBackgroundResource(R.drawable.tblue);
                yellowButton.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        redButton.setBackgroundResource(R.drawable.rblank);
                        greenButton.setBackgroundResource(R.drawable.fblank);
                        yellowButton.setBackgroundResource(R.drawable.oblank);
                        blueButton.setBackgroundResource(R.drawable.tblank);
                        playSequenceAnimations();
                    }
                }, 3000);


                animationInProgress = false;
            }
        });
        dropAnimator.start();
    }


    private void startFadeOutAnimation() {
        if (animationInProgress) return;
        animationInProgress = true;
        ObjectAnimator fadeOutAnimator = ObjectAnimator.ofFloat(weaponView, "alpha", 1f, 0f);
        fadeOutAnimator.setDuration(2000);
        fadeOutAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        fadeOutAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animationInProgress = false;
                newRound();
            }
        });
        fadeOutAnimator.start();
    }

    private void startFadeAnimation(int color) {
        if (animationInProgress) return;
        animationInProgress = true;
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), Color.TRANSPARENT, color);
        colorAnimation.setDuration(500);

        colorAnimation.setRepeatCount(ValueAnimator.RESTART);
        colorAnimation.setRepeatMode(ValueAnimator.REVERSE);

        colorAnimation.addUpdateListener(animator -> {
            int animatedColor = (int) animator.getAnimatedValue();
            weaponView.setColorFilter(animatedColor);
        });

        colorAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animationInProgress = false;
                weaponView.setColorFilter(null);
                Log.d("MainActivity", "Coloring ended");
            }
        });

        colorAnimation.start();
    }

    private void startSuccessAnimationIn() {
        disableButtons();

        if (animationInProgress) return;
        animationInProgress = true;
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), Color.TRANSPARENT, Color.WHITE);
        colorAnimation.setDuration(1000);

        colorAnimation.addUpdateListener(animator -> {
            int animatedColor = (int) animator.getAnimatedValue();
            weaponView.setColorFilter(animatedColor);
        });

        colorAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animationInProgress = false;
                startSuccessAnimationOut();
            }
        });

        colorAnimation.start();
    }

    private void startSuccessAnimationOut() {
        weaponView.setImageResource(getResources().getIdentifier(weaponName + "enchanted", "drawable", getPackageName()));

        if (animationInProgress) return;
        animationInProgress = true;
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), Color.WHITE, Color.TRANSPARENT);
        colorAnimation.setDuration(1000);

        colorAnimation.addUpdateListener(animator -> {
            int animatedColor = (int) animator.getAnimatedValue();
            weaponView.setColorFilter(animatedColor);
        });

        colorAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animationInProgress = false;

                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                startFadeOutAnimation();
            }
        });

        colorAnimation.start();
    }

    private void startGameOverAnimationIn(){
        disableButtons();

        if (animationInProgress) return;
        animationInProgress = true;
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), Color.TRANSPARENT, Color.BLACK);
        colorAnimation.setDuration(1000);

        colorAnimation.addUpdateListener(animator -> {
            int animatedColor = (int) animator.getAnimatedValue();
            weaponView.setColorFilter(animatedColor);
        });

        colorAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animationInProgress = false;
                startGameOverAnimationOut();
            }
        });

        colorAnimation.start();
    }

    private void startGameOverAnimationOut(){
        weaponView.setImageResource(getResources().getIdentifier(weaponName + "broken", "drawable", getPackageName()));

        if (animationInProgress) return;
        animationInProgress = true;
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), Color.BLACK, Color.TRANSPARENT);
        colorAnimation.setDuration(1000);

        colorAnimation.addUpdateListener(animator -> {
            int animatedColor = (int) animator.getAnimatedValue();
            weaponView.setColorFilter(animatedColor);
        });

        colorAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animationInProgress = false;

                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                gameOver();
            }
        });

        colorAnimation.start();
    }

    private void disableButtons(){
        redButton.setEnabled(false);
        greenButton.setEnabled(false);
        yellowButton.setEnabled(false);
        blueButton.setEnabled(false);
    }

    private void enableButtons(){
        redButton.setEnabled(true);
        greenButton.setEnabled(true);
        yellowButton.setEnabled(true);
        blueButton.setEnabled(true);
    }

    private void playSequenceAnimations() {
        final Handler handler = new Handler();

        for (int i = 0; i < sequence.size(); i++) {
            final int index = i;

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ColorButton colorButton = sequence.get(index);
                    ButtonColor color = colorButton.color;
                    int colorValue = 0;

                    switch (color) {
                        case RED:
                            colorValue = RED;
                            break;
                        case YELLOW:
                            colorValue = YELLOW;
                            break;
                        case BLUE:
                            colorValue = BLUE;
                            break;
                        case GREEN:
                            colorValue = GREEN;
                            break;
                    }
                    startFadeAnimation(colorValue);
                    Log.d("MainActivity", "Coloring start " + Color.valueOf(colorValue));
                }
            }, i * 2000);
        }
        enableButtons();
    }
}