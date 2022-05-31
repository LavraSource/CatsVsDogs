package com.example.lamps;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.view.MotionEvent;
import android.view.View;

import java.util.Random;

public class GameView extends View {
    private static final String APP_PREFERENCES = "stats";
    static int N = 3;
    int r;
    int m;
    int size;
    int offset;
    boolean isCalc=false;
    boolean hasWon = false;
    Bitmap dog;
    Bitmap cat;
    Bitmap youwon;
    double touchX=0,touchY=0;
    Random rand=new Random();

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    SoundPool mSoundPool;
    int meowId;
    int woofId;
    MediaPlayer catsong;
    MediaPlayer dogsong;


    boolean[][] field= new boolean[N][N];

    Paint paint = new Paint();

    public GameView(Context context) {
        super(context);
        sharedPreferences = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        for(int i =0; i<N; i++){
            for(int j =0; j<N; j++){
                field[i][j]=rand.nextBoolean();
            }
        }
        editor = sharedPreferences.edit();
        mSoundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
        meowId = mSoundPool.load(context, R.raw.meow, 1);
        woofId = mSoundPool.load(context, R.raw.woof, 1);
        catsong=MediaPlayer.create(getContext(), R.raw.catsong);
        dogsong=MediaPlayer.create(getContext(), R.raw.dogsong);

    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawColor(Color.rgb(20,61,20));
        if(!isCalc) {
            size=canvas.getWidth() / N;
            r = (int) (size * 0.8) / 2;
            paint.setStrokeWidth(r / 10);
            isCalc=!isCalc;
            offset=canvas.getHeight()/2-canvas.getWidth()/4*3;
            dog = BitmapFactory.decodeResource(getResources(), R.drawable.dog);
            cat = BitmapFactory.decodeResource(getResources(), R.drawable.cat);
            youwon = BitmapFactory.decodeResource(getResources(), R.drawable.you_won);
            dog = Bitmap.createScaledBitmap(dog, size, size, false);
            cat = Bitmap.createScaledBitmap(cat, size, size, false);
            youwon = Bitmap.createScaledBitmap(youwon, size*2, size/2, false);
        }
        paint.setColor(Color.rgb(40,123,40));
        canvas.drawRect(0, offset, canvas.getWidth(), canvas.getWidth()+offset, paint);
        hasWon=true;
        paint.setColor(Color.YELLOW);
        boolean sp=field[0][0];
        if(!sharedPreferences.contains("wins")){
            editor.putInt("wins", 0);
            editor.apply();
        }
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {

                paint.setStyle(Paint.Style.FILL);
                if(hasWon&&field[j][i]!=sp){
                    hasWon=false;
                }
                if(field[j][i]){
                    canvas.drawBitmap(dog, size*i, size*j+offset, paint);
                } else {
                    canvas.drawBitmap(cat, size*i, size*j+offset, paint);
                }
            }
        }
        paint.setTextSize(getWidth()/10);
        if(hasWon){
            if(sp){
                if(!dogsong.isPlaying()){
                    dogsong.start();
                }
            } else {
                if(!catsong.isPlaying()){
                    catsong.start();
                }
            }
            canvas.drawBitmap(youwon, (int)(size*0.5), (int)(offset+size*3.2), paint);
            editor.putInt("wins", sharedPreferences.getInt("wins", 0)+1);
            editor.apply();
        }
        paint.setColor(Color.rgb(125,255,125));
        canvas.drawText("Wins: "+sharedPreferences.getInt("wins", 0), (int)(70), (int)(offset+size*4.5), paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!hasWon) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                touchX = event.getX();
                touchY = event.getY();
                choiceCalc();
                invalidate();
            }
        } else {
            if(catsong.isPlaying()){
                catsong.stop();
            }
            if(dogsong.isPlaying()){
                dogsong.stop();
            }
            getActivity().finish();
        }

        return super.onTouchEvent(event);

    }

    private void choiceCalc() {
        int i = Math.min(Math.max((int) ((touchX)/size),0),N-1);
        int j = Math.min(Math.max((int) ((touchY-offset)/size),0),N-1);
        int x0 = (size/2)*(i*2+1);
        int y0 = (size/2)*(j*2+1)+offset;
        if((touchX-x0)*(touchX-x0)+(touchY-y0)*(touchY-y0)<=r*r+(r/10)*(r/10)){
            if(j+1<N){
                field[j+1][i]=!field[j+1][i];
            }
            if(j-1>=0){
                field[j-1][i]=!field[j-1][i];
            }
            if(i+1<N){
                field[j][i+1]=!field[j][i+1];
            }
            if(i-1>=0){
                field[j][i-1]=!field[j][i-1];
            }
            AudioManager audioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
            float curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            float maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            float leftVolume = curVolume / maxVolume;
            float rightVolume = curVolume / maxVolume;
            int priority = 1;
            int no_loop = 0;
            float normal_playback_rate = 0.7f + (float)rand.nextInt(6)/10;
            if(field[j][i]) {
                int mStreamId = mSoundPool.play(woofId, leftVolume, rightVolume, priority, no_loop,
                        normal_playback_rate);
            } else {
                int mStreamId = mSoundPool.play(meowId, leftVolume/10, rightVolume/10, priority, no_loop,
                        normal_playback_rate);
            }
            field[j][i]=!field[j][i];
        }

    }
    private Activity getActivity() {
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }
}
