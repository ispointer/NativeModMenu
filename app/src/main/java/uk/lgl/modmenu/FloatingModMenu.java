//
// Created by aantik on 2/1/2026.
//

package uk.lgl.modmenu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.util.Base64;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.widget.RelativeLayout.ALIGN_PARENT_LEFT;
import static android.widget.RelativeLayout.ALIGN_PARENT_RIGHT;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class FloatingModMenu {


    public static final String TAG = "Mod_Menu"; //Tag for logcat
    int TEXT_COLOR = Color.parseColor("#82CAFD");
    int TEXT_COLOR_2 = Color.parseColor("#FFFFFF");
    int BTN_COLOR = Color.parseColor("#1C262D");
    int MENU_BG_COLOR = Color.parseColor("#EE1C2A35"); //#AARRGGBB
    int MENU_FEATURE_BG_COLOR = Color.parseColor("#DD141C22"); //#AARRGGBB
    int MENU_WIDTH = 290;
    int MENU_HEIGHT = 210;
    float MENU_CORNER = 4f;
    int ICON_SIZE = 45; //Change both width and height of image
    float ICON_ALPHA = 0.7f; //Transparent
    int ToggleON = Color.GREEN;
    int ToggleOFF = Color.RED;
    int BtnON = Color.parseColor("#1b5e20");
    int BtnOFF = Color.parseColor("#7f0000");
    int CategoryBG = Color.parseColor("#2F3D4C");
    int SeekBarColor = Color.parseColor("#80CBC4");
    int SeekBarProgressColor = Color.parseColor("#80CBC4");
    int CheckBoxColor = Color.parseColor("#80CBC4");
    int RadioColor = Color.parseColor("#FFFFFF");
    String NumberTxtColor = "#41c300";
    RelativeLayout mCollapsed, mRootContainer;
    LinearLayout mExpanded, patches, mSettings, mCollapse;
    LinearLayout.LayoutParams scrlLLExpanded, scrlLL;
    WindowManager mWindowManager;
    WindowManager.LayoutParams params;
    ImageView startimage;
    FrameLayout rootFrame;
    ScrollView scrollView;
    private Activity activity;
    boolean stopChecking;

    //initialize methods from the native library
    native void setTitleText(TextView textView);

    native void setHeadingText(TextView textView);

    native String Icon();

    native String IconWebViewData();

    native String[] getFeatureList();

    native String[] settingsList();

    public static native void Changes(Context con, int fNum, String fName, int i, boolean bool, String str);


    public FloatingModMenu(Activity activity) {
        this.activity = activity;
        initFloating();
    }


    private static FloatingModMenu instance;

    public static void CreateMenu(final Context context) {
        if (instance != null) return;
        if (!(context instanceof Activity)) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Activity activity = getTopActivity();
                    if (activity != null) {
                        instance = new FloatingModMenu(activity);
                    }
                }
            });
        } else {
            instance = new FloatingModMenu((Activity) context);
        }
    }

    private static Activity getTopActivity() {
        try {
            Class<?> at = Class.forName("android.app.ActivityThread");
            Method current = at.getDeclaredMethod("currentActivityThread");
            current.setAccessible(true);
            Object atObj = current.invoke(null);

            Field activities = at.getDeclaredField("mActivities");
            activities.setAccessible(true);
            Map<?, ?> map = (Map<?, ?>) activities.get(atObj);

            for (Object record : map.values()) {
                Class<?> ar = record.getClass();
                Field paused = ar.getDeclaredField("paused");
                paused.setAccessible(true);
                if (!paused.getBoolean(record)) {
                    Field activity = ar.getDeclaredField("activity");
                    activity.setAccessible(true);
                    return (Activity) activity.get(record);
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }



    @SuppressLint({"WrongConstant", "ClickableViewAccessibility", "ObsoleteSdkInt", "SetTextI18n"})
    private void initFloating() {
        rootFrame = new FrameLayout(activity);
        rootFrame.setOnTouchListener(onTouchListener());
        mRootContainer = new RelativeLayout(activity);
        mCollapsed = new RelativeLayout(activity);
        mCollapsed.setVisibility(View.VISIBLE);
        mCollapsed.setAlpha(ICON_ALPHA);

        //********** The box of the mod menu **********
        mExpanded = new LinearLayout(activity);
        mExpanded.setVisibility(View.GONE);
        mExpanded.setBackgroundColor(MENU_BG_COLOR);
        mExpanded.setOrientation(LinearLayout.VERTICAL);
        mExpanded.setLayoutParams(new LinearLayout.LayoutParams(dp(MENU_WIDTH), WRAP_CONTENT));
        GradientDrawable gdMenuBody = new GradientDrawable();
        gdMenuBody.setCornerRadius(MENU_CORNER);
        gdMenuBody.setColor(MENU_BG_COLOR);
        gdMenuBody.setStroke(1, Color.parseColor("#32cb00"));


        startimage = new ImageView(activity);
        startimage.setLayoutParams(new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        int applyDimension = (int) TypedValue.applyDimension(1, ICON_SIZE, activity.getResources().getDisplayMetrics()); //Icon size
        startimage.getLayoutParams().height = applyDimension;
        startimage.getLayoutParams().width = applyDimension;
        //startimage.requestLayout();
        startimage.setScaleType(ImageView.ScaleType.FIT_XY);
        byte[] decode = Base64.decode(Icon(), 0);
        startimage.setImageBitmap(BitmapFactory.decodeByteArray(decode, 0, decode.length));
        ((ViewGroup.MarginLayoutParams) startimage.getLayoutParams()).topMargin = convertDipToPixels(10);
        //Initialize event handlers for buttons, etc.
        startimage.setOnTouchListener(onTouchListener());
        startimage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mCollapsed.setVisibility(View.GONE);
                mExpanded.setVisibility(View.VISIBLE);
            }
        });

        //********** The icon in Webview to open mod menu **********
        WebView wView = new WebView(activity); //Icon size width=\"50\" height=\"50\"
        wView.setLayoutParams(new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        int applyDimension2 = (int) TypedValue.applyDimension(1, ICON_SIZE, activity.getResources().getDisplayMetrics()); //Icon size
        wView.getLayoutParams().height = applyDimension2;
        wView.getLayoutParams().width = applyDimension2;
        wView.loadData("<html>" +
                "<head></head>" +
                "<body style=\"margin: 0; padding: 0\">" +
                "<img src=\"" + IconWebViewData() + "\" width=\"" + ICON_SIZE + "\" height=\"" + ICON_SIZE + "\" >" +
                "</body>" +
                "</html>", "text/html", "utf-8");
        wView.setBackgroundColor(0x00000000); //Transparent
        wView.setAlpha(ICON_ALPHA);
        wView.setOnTouchListener(onTouchListener());

        TextView settings = new TextView(activity);
        settings.setText(Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M ? "⚙" : "\uD83D\uDD27");
        settings.setTextColor(TEXT_COLOR);
        settings.setTypeface(Typeface.DEFAULT_BOLD);
        settings.setTextSize(20.0f);
        RelativeLayout.LayoutParams rlsettings = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        rlsettings.addRule(ALIGN_PARENT_RIGHT);
        settings.setLayoutParams(rlsettings);
        settings.setOnClickListener(new View.OnClickListener() {
            boolean settingsOpen;

            @Override
            public void onClick(View v) throws IllegalStateException {
                settingsOpen = !settingsOpen;
                if (settingsOpen) {
                    scrollView.removeView(patches);
                    scrollView.addView(mSettings);
                    scrollView.scrollTo(0, 0);
                } else {
                    scrollView.removeView(mSettings);
                    scrollView.addView(patches);
                }
            }
        });

        //********** Settings **********
        mSettings = new LinearLayout(activity);
        mSettings.setOrientation(LinearLayout.VERTICAL);
        featureList(settingsList(), mSettings);

        //********** Title text **********
        RelativeLayout titleText = new RelativeLayout(activity);
        titleText.setPadding(10, 5, 10, 5);
        titleText.setVerticalGravity(16);
        TextView title = new TextView(activity);
        title.setTextColor(TEXT_COLOR);
        title.setTextSize(18.0f);
        title.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        rl.addRule(RelativeLayout.CENTER_HORIZONTAL);
        title.setLayoutParams(rl);
        setTitleText(title);

        //********** Heading text **********
        TextView heading = new TextView(activity);
        heading.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        heading.setMarqueeRepeatLimit(-1);
        heading.setSingleLine(true);
        heading.setSelected(true);
        heading.setTextColor(TEXT_COLOR);
        heading.setTextSize(10.0f);
        heading.setGravity(Gravity.CENTER);
        heading.setPadding(0, 0, 0, 5);
        setHeadingText(heading);


        scrollView = new ScrollView(activity);
        //Auto size. To set size manually, change the width and height example 500, 500
        scrlLL = new LinearLayout.LayoutParams(MATCH_PARENT, dp(MENU_HEIGHT));
        scrlLLExpanded = new LinearLayout.LayoutParams(mExpanded.getLayoutParams());
        scrlLLExpanded.weight = 1.0f;
        scrollView.setLayoutParams(scrlLL);
        scrollView.setBackgroundColor(MENU_FEATURE_BG_COLOR);
        patches = new LinearLayout(activity);
        patches.setOrientation(LinearLayout.VERTICAL);

        //********** RelativeLayout for buttons **********
        RelativeLayout relativeLayout = new RelativeLayout(activity);
        relativeLayout.setPadding(10, 3, 10, 3);
        relativeLayout.setVerticalGravity(Gravity.CENTER);

        //**********  Hide/Kill button **********
        RelativeLayout.LayoutParams lParamsHideBtn = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        lParamsHideBtn.addRule(ALIGN_PARENT_LEFT);

        Button hideBtn = new Button(activity);
        hideBtn.setLayoutParams(lParamsHideBtn);
        hideBtn.setBackgroundColor(Color.TRANSPARENT);
        hideBtn.setText("HIDE/KILL (Hold)");
        hideBtn.setTextColor(TEXT_COLOR);
        hideBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mCollapsed.setVisibility(View.VISIBLE);
                mCollapsed.setAlpha(0);
                mExpanded.setVisibility(View.GONE);
                Toast.makeText(view.getContext(), "Icon hidden. Remember the hidden icon position", Toast.LENGTH_LONG).show();
            }
        });
        hideBtn.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
                Toast.makeText(view.getContext(), "Menu service killed", Toast.LENGTH_LONG).show();
                mWindowManager.removeView(rootFrame);
                return false;
            }
        });

        //********** Close button **********
        RelativeLayout.LayoutParams lParamsCloseBtn = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        lParamsCloseBtn.addRule(ALIGN_PARENT_RIGHT);

        Button closeBtn = new Button(activity);
        closeBtn.setLayoutParams(lParamsCloseBtn);
        closeBtn.setBackgroundColor(Color.TRANSPARENT);
        closeBtn.setText("MINIMIZE");
        closeBtn.setTextColor(TEXT_COLOR);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                mCollapsed.setVisibility(View.VISIBLE);
                mCollapsed.setAlpha(ICON_ALPHA);
                mExpanded.setVisibility(View.GONE);
            }
        });

        //********** Params **********
        //Variable to check later if the phone supports Draw over other apps permission

        params = new WindowManager.LayoutParams(
                WRAP_CONTENT,
                WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
                PixelFormat.TRANSPARENT
        );
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;


        //********** Adding view components **********
        rootFrame.addView(mRootContainer);
        mRootContainer.addView(mCollapsed);
        mRootContainer.addView(mExpanded);
        if (IconWebViewData() != null) {
            mCollapsed.addView(wView);
        } else {
            mCollapsed.addView(startimage);
        }
        titleText.addView(title);
        titleText.addView(settings);
        mExpanded.addView(titleText);
        mExpanded.addView(heading);
        scrollView.addView(patches);
        mExpanded.addView(scrollView);
        relativeLayout.addView(hideBtn);
        relativeLayout.addView(closeBtn);
        mExpanded.addView(relativeLayout);
        mWindowManager = activity.getWindowManager();
        mWindowManager.addView(rootFrame, params);
        patches.removeAllViews();
        featureList(getFeatureList(), patches);
    }

    private View.OnTouchListener onTouchListener() {
        return new View.OnTouchListener() {
            final View collapsedView = mCollapsed;
            final View expandedView = mExpanded;
            private float initialTouchX, initialTouchY;
            private int initialX, initialY;

            @SuppressLint("ClickableViewAccessibility")
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = motionEvent.getRawX();
                        initialTouchY = motionEvent.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        int rawX = (int) (motionEvent.getRawX() - initialTouchX);
                        int rawY = (int) (motionEvent.getRawY() - initialTouchY);
                        mExpanded.setAlpha(1f);
                        mCollapsed.setAlpha(1f);
                        if (rawX < 10 && rawY < 10 && isViewCollapsed()) {
                            try {
                                collapsedView.setVisibility(View.GONE);
                                expandedView.setVisibility(View.VISIBLE);
                            } catch (NullPointerException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        mExpanded.setAlpha(0.5f);
                        mCollapsed.setAlpha(0.5f);
                        //Calculate the X and Y coordinates of the view.
                        params.x = initialX + ((int) (motionEvent.getRawX() - initialTouchX));
                        params.y = initialY + ((int) (motionEvent.getRawY() - initialTouchY));
                        //Update the layout with new X & Y coordinate
                        mWindowManager.updateViewLayout(rootFrame, params);
                        return true;
                    default:
                        return false;
                }
            }
        };
    }


    private void featureList(String[] listFT, LinearLayout linearLayout) {
        //Currently looks messy right now. Let me know if you have improvements
        int featNum, subFeat = 0;
        LinearLayout llBak = linearLayout;

        for (int i = 0; i < listFT.length; i++) {
            boolean switchedOn = false;
            //Log.i("featureList", listFT[i]);
            String feature = listFT[i];
            if (feature.contains("True_")) {
                switchedOn = true;
                feature = feature.replaceFirst("True_", "");
            }

            linearLayout = llBak;
            if (feature.contains("CollapseAdd_")) {
                //if (collapse != null)
                linearLayout = mCollapse;
                feature = feature.replaceFirst("CollapseAdd_", "");
            }
            String[] str = feature.split("_");

            //Assign feature number
            if (TextUtils.isDigitsOnly(str[0]) || str[0].matches("-[0-9]*")) {
                featNum = Integer.parseInt(str[0]);
                feature = feature.replaceFirst(str[0] + "_", "");
                subFeat++;
            } else {
                //Subtract feature number. We don't want to count ButtonLink, Category, RichTextView and RichWebView
                featNum = i - subFeat;
            }
            String[] strSplit = feature.split("_");
            switch (strSplit[0]) {
                case "Toggle":
                    linearLayout.addView(Switch(featNum, strSplit[1], switchedOn));
                    break;
                case "SeekBar":
                    linearLayout.addView(SeekBar(featNum, strSplit[1], Integer.parseInt(strSplit[2]), Integer.parseInt(strSplit[3])));
                    break;
                case "Button":
                    linearLayout.addView(Button(featNum, strSplit[1]));
                    break;
                case "ButtonOnOff":
                    linearLayout.addView(ButtonOnOff(featNum, strSplit[1], switchedOn));
                    break;
                case "Spinner":
                    linearLayout.addView(RichTextView(strSplit[1]));
                    linearLayout.addView(Spinner(featNum, strSplit[1], strSplit[2]));
                    break;
                case "InputText":
                    linearLayout.addView(TextField(featNum, strSplit[1], false, 0));
                    break;
                case "InputValue":
                    if (strSplit.length == 3)
                        linearLayout.addView(TextField(featNum, strSplit[2], true, Integer.parseInt(strSplit[1])));
                    if (strSplit.length == 2)
                        linearLayout.addView(TextField(featNum, strSplit[1], true, 0));
                    break;
                case "CheckBox":
                    linearLayout.addView(CheckBox(featNum, strSplit[1], switchedOn));
                    break;
                case "RadioButton":
                    linearLayout.addView(RadioButton(featNum, strSplit[1], strSplit[2]));
                    break;
                case "Collapse":
                    Collapse(linearLayout, strSplit[1]);
                    subFeat++;
                    break;
                case "ButtonLink":
                    subFeat++;
                    linearLayout.addView(ButtonLink(strSplit[1], strSplit[2]));
                    break;
                case "Category":
                    subFeat++;
                    linearLayout.addView(Category(strSplit[1]));
                    break;
                case "RichTextView":
                    subFeat++;
                    linearLayout.addView(RichTextView(strSplit[1]));
                    break;
                case "RichWebView":
                    subFeat++;
                    linearLayout.addView(RichWebView(strSplit[1]));
                    break;
            }
        }
    }

    private View Switch(final int featNum, final String featName, boolean swiOn) {
        final Switch switchR = new Switch(activity);
        ColorStateList buttonStates = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_enabled},
                        new int[]{android.R.attr.state_checked},
                        new int[]{}
                },
                new int[]{
                        Color.BLUE,
                        ToggleON, // ON
                        ToggleOFF // OFF
                }
        );
        //Set colors of the switch. Comment out if you don't like it
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            switchR.getThumbDrawable().setTintList(buttonStates);
            switchR.getTrackDrawable().setTintList(buttonStates);
        }
        switchR.setText(featName);
        switchR.setTextColor(TEXT_COLOR_2);
        switchR.setPadding(10, 5, 0, 5);
        switchR.setChecked(swiOn);
        switchR.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean bool) {
                switch (featNum) {
                    case -1: //Save perferences
                        if (bool == false)
                            break;
                    case -3:
                        scrollView.setLayoutParams(bool ? scrlLLExpanded : scrlLL);
                        break;
                }
            }
        });
        return switchR;
    }

    private View SeekBar(final int featNum, final String featName, final int min, final int max) {
        // Remove loadedProg since Changes is void
        // Call Changes just to initialize if needed
        Changes(activity, featNum, featName, min, false, "");

        LinearLayout linearLayout = new LinearLayout(activity);
        linearLayout.setPadding(10, 5, 0, 5);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);

        final TextView textView = new TextView(activity);
        textView.setText(Html.fromHtml(featName + ": <font color='" + NumberTxtColor + "'>" + min + "</font>"));
        textView.setTextColor(TEXT_COLOR_2);

        SeekBar seekBar = new SeekBar(activity);
        seekBar.setPadding(25, 10, 35, 10);
        seekBar.setMax(max);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            seekBar.setMin(min); // setMin for Oreo and above
        seekBar.setProgress(min);
        seekBar.getThumb().setColorFilter(SeekBarColor, PorterDuff.Mode.SRC_ATOP);
        seekBar.getProgressDrawable().setColorFilter(SeekBarProgressColor, PorterDuff.Mode.SRC_ATOP);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // optional
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // optional
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int finalProgress = progress < min ? min : progress;
                seekBar.setProgress(finalProgress);

                // Call Changes with 6 parameters
                Changes(activity, featNum, featName, finalProgress, false, "");

                // Update TextView
                textView.setText(Html.fromHtml(featName + ": <font color='" + NumberTxtColor + "'>" + finalProgress + "</font>"));
            }
        });

        linearLayout.addView(textView);
        linearLayout.addView(seekBar);

        return linearLayout;
    }


    private View Button(final int featNum, final String featName) {
        final Button button = new Button(activity);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        layoutParams.setMargins(7, 5, 7, 5);
        button.setLayoutParams(layoutParams);
        button.setTextColor(TEXT_COLOR_2);
        button.setAllCaps(false); //Disable caps to support html
        button.setText(Html.fromHtml(featName));
        button.setBackgroundColor(BTN_COLOR);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                switch (featNum) {
                    case -4:
                        break;
                    case -5:
                        break;
                    case -6:
                        scrollView.removeView(mSettings);
                        scrollView.addView(patches);
                        break;
                    case -100:
                        stopChecking = true;
                        break;
                }
                Changes(activity, featNum, featName, 0, false, "");

            }
        });

        return button;
    }

    private View ButtonLink(final String featName, final String url) {
        final Button button = new Button(activity);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        layoutParams.setMargins(7, 5, 7, 5);
        button.setLayoutParams(layoutParams);
        button.setAllCaps(false); //Disable caps to support html
        button.setTextColor(TEXT_COLOR_2);
        button.setText(Html.fromHtml(featName));
        button.setBackgroundColor(BTN_COLOR);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse(url));
                activity.startActivity(intent);
            }
        });
        return button;
    }

    private View ButtonOnOff(final int featNum, String featName, boolean switchedOn) {
        final Button button = new Button(activity);

        // Layout setup
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT // Changed from MATCH_PARENT for better button size
        );
        layoutParams.setMargins(7, 5, 7, 5);
        button.setLayoutParams(layoutParams);
        button.setTextColor(TEXT_COLOR_2);
        button.setAllCaps(false); // Disable caps to support HTML

        final String finalFeatName = featName.replace("OnOff_", "");

        // Local state
        final boolean[] isOn = {switchedOn}; // use array to allow modification inside OnClickListener

        // Initialize button appearance
        if (isOn[0]) {
            button.setText(Html.fromHtml(finalFeatName + ": ON"));
            button.setBackgroundColor(BtnON);
        } else {
            button.setText(Html.fromHtml(finalFeatName + ": OFF"));
            button.setBackgroundColor(BtnOFF);
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call your native method
                // Fill in placeholders for int and String parameters your native method requires
                Changes(activity, featNum, finalFeatName, 0, isOn[0], "");

                // Toggle local state
                isOn[0] = !isOn[0];

                // Update button text and color
                if (isOn[0]) {
                    button.setText(Html.fromHtml(finalFeatName + ": ON"));
                    button.setBackgroundColor(BtnON);
                } else {
                    button.setText(Html.fromHtml(finalFeatName + ": OFF"));
                    button.setBackgroundColor(BtnOFF);
                }
            }
        });

        return button;
    }


    private View Spinner(final int featNum, final String featName, final String list) {
        final List<String> lists = new LinkedList<>(Arrays.asList(list.split(",")));

        // Create LinearLayout wrapper
        LinearLayout linearLayout2 = new LinearLayout(activity);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams2.setMargins(7, 2, 7, 5);
        linearLayout2.setOrientation(LinearLayout.VERTICAL);
        linearLayout2.setBackgroundColor(BTN_COLOR);
        linearLayout2.setLayoutParams(layoutParams2);

        final Spinner spinner = new Spinner(activity, Spinner.MODE_DROPDOWN);
        spinner.setLayoutParams(layoutParams2);

        // Proper color for arrow
        spinner.getBackground().setColorFilter(TEXT_COLOR_2, PorterDuff.Mode.SRC_ATOP);

        ArrayAdapter<String> aa = new ArrayAdapter<>(activity,
                android.R.layout.simple_spinner_dropdown_item, lists);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(aa);

        // Set initial selection based on featName
        int initialPos = lists.indexOf(featName);
        if (initialPos >= 0) {
            spinner.setSelection(initialPos);
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selected = spinner.getSelectedItem().toString();

                // Call native method (fill dummy values for missing parameters for now)
                Changes(activity, featNum, selected, position, true, "");

                // Change text color of selected item
                if (parentView.getChildAt(0) instanceof TextView) {
                    ((TextView) parentView.getChildAt(0)).setTextColor(TEXT_COLOR_2);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        linearLayout2.addView(spinner);
        return linearLayout2;
    }


    private View TextField(final int featNum, final String featName, final boolean numOnly, final int maxValue) {
        final EditTextString edittextstring = new EditTextString();
        final EditTextNum edittextnum = new EditTextNum();
        LinearLayout linearLayout = new LinearLayout(activity);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        layoutParams.setMargins(7, 5, 7, 5);

        final Button button = new Button(activity);
        if (numOnly) {
            int num = 0;
            edittextnum.setNum((num == 0) ? 1 : num);
            button.setText(Html.fromHtml(featName + ": <font color='" + NumberTxtColor + "'>" + ((num == 0) ? 1 : num) + "</font>"));
        } else {
            String string = "";
            edittextstring.setString((string == "") ? "" : string);
            button.setText(Html.fromHtml(featName + ": <font color='" + NumberTxtColor + "'>" + string + "</font>"));
        }
        button.setAllCaps(false);
        button.setLayoutParams(layoutParams);
        button.setBackgroundColor(BTN_COLOR);
        button.setTextColor(TEXT_COLOR_2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               Toast.makeText(activity,  "Button clicked", Toast.LENGTH_SHORT).show();
                final AlertDialog alert = new AlertDialog.Builder(activity, 2).create();
                alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @SuppressLint("WrongConstant")
                    public void onCancel(DialogInterface dialog) {
                        InputMethodManager imm = (InputMethodManager) activity.getSystemService(activity.INPUT_METHOD_SERVICE);
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                    }
                });

                //LinearLayout
                LinearLayout linearLayout1 = new LinearLayout(activity);
                linearLayout1.setPadding(5, 5, 5, 5);
                linearLayout1.setOrientation(LinearLayout.VERTICAL);
                linearLayout1.setBackgroundColor(MENU_FEATURE_BG_COLOR);

                //TextView
                final TextView TextViewNote = new TextView(activity);
                TextViewNote.setText("Tap OK to apply changes. Tap outside to cancel");
                if (maxValue != 0)
                    TextViewNote.setText("Tap OK to apply changes. Tap outside to cancel\nMax value: " + maxValue);
                TextViewNote.setTextColor(TEXT_COLOR_2);

                //Edit text
                final EditText edittext = new EditText(activity);
                edittext.setMaxLines(1);
                edittext.setWidth(convertDipToPixels(300));
                edittext.setTextColor(TEXT_COLOR_2);
                if (numOnly) {
                    edittext.setInputType(InputType.TYPE_CLASS_NUMBER);
                    edittext.setKeyListener(DigitsKeyListener.getInstance("0123456789-"));
                    InputFilter[] FilterArray = new InputFilter[1];
                    FilterArray[0] = new InputFilter.LengthFilter(10);
                    edittext.setFilters(FilterArray);
                } else {
                    edittext.setText(edittextstring.getString());
                }
                edittext.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @SuppressLint("WrongConstant")
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        InputMethodManager imm = (InputMethodManager) activity.getSystemService(activity.INPUT_METHOD_SERVICE);
                        if (hasFocus) {
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                        } else {
                            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        }
                    }
                });
                edittext.requestFocus();

                //Button
                Button btndialog = new Button(activity);
                btndialog.setBackgroundColor(BTN_COLOR);
                btndialog.setTextColor(TEXT_COLOR_2);
                btndialog.setText("OK");
                btndialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (numOnly) {
                            int num;
                            try {
                                num = Integer.parseInt(TextUtils.isEmpty(edittext.getText().toString()) ? "0" : edittext.getText().toString());
                                if (maxValue != 0 &&  num >= maxValue)
                                    num = maxValue;
                            } catch (NumberFormatException ex) {
                                num = 2147483640;
                            }
                            edittextnum.setNum(num);
                            button.setText(Html.fromHtml(featName + ": <font color='" + NumberTxtColor + "'>" + num + "</font>"));
                            alert.dismiss();
                            Changes(activity, featNum, featName, num, false, "");
                           // Preferences.changeFeatureInt(featName, featNum, num);
                        } else {
                            String str = edittext.getText().toString();
                            edittextstring.setString(edittext.getText().toString());
                            button.setText(Html.fromHtml(featName + ": <font color='" + NumberTxtColor + "'>" + str + "</font>"));
                            alert.dismiss();
                            Changes(activity, featNum, featName, 0, false, str);
                            //Preferences.changeFeatureString(featName, featNum, str);
                        }
                        edittext.setFocusable(false);
                    }
                });

                linearLayout1.addView(TextViewNote);
                linearLayout1.addView(edittext);
                linearLayout1.addView(btndialog);
                alert.setView(linearLayout1);
                alert.show();
            }
        });

        linearLayout.addView(button);
        return linearLayout;
    }



    private View CheckBox(final int featNum, final String featName, boolean switchedOn) {
        final CheckBox checkBox = new CheckBox(activity);
        checkBox.setText(featName);
        checkBox.setTextColor(TEXT_COLOR_2);

        // Set button tint for Lollipop and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            checkBox.setButtonTintList(ColorStateList.valueOf(CheckBoxColor));
        }

        // Set initial checked state
        checkBox.setChecked(switchedOn);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Corrected call with all 6 parameters
                Changes(activity, featNum, featName, 0, isChecked, "");
            }
        });

        return checkBox;
    }


    // Map to store selected index for each feature


    private View RadioButton(final int featNum, final String featName, final String list) {
        final List<String> options = new LinkedList<>(Arrays.asList(list.split(",")));
        Map<Integer, Integer> savedIndexes = new HashMap<>();
        final TextView textView = new TextView(activity);
        textView.setText(featName + ":");
        textView.setTextColor(TEXT_COLOR_2);

        final RadioGroup radioGroup = new RadioGroup(activity);
        radioGroup.setPadding(10, 5, 10, 5);
        radioGroup.setOrientation(LinearLayout.VERTICAL);
        radioGroup.addView(textView);

        for (int i = 0; i < options.size(); i++) {
            final RadioButton radioButton = new RadioButton(activity);
            final String optionName = options.get(i);
            final int index = i;

            radioButton.setText(optionName);
            radioButton.setTextColor(Color.LTGRAY);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                radioButton.setButtonTintList(ColorStateList.valueOf(RadioColor));

            radioButton.setOnClickListener(v -> {
                textView.setText(Html.fromHtml(featName + ": <font color='" + NumberTxtColor + "'>" + optionName));
                Changes(activity, featNum, featName, index, false, "");
                savedIndexes.put(featNum, index);
            });

            radioGroup.addView(radioButton);
        }
        int savedIndex = savedIndexes.getOrDefault(featNum, -1);
        if (savedIndex >= 0 && savedIndex < radioGroup.getChildCount()) {
            RadioButton savedRadio = (RadioButton) radioGroup.getChildAt(savedIndex + 1); // +1 because first child is TextView
            savedRadio.setChecked(true);
            textView.setText(Html.fromHtml(featName + ": <font color='" + NumberTxtColor + "'>" + options.get(savedIndex)));
        }

        return radioGroup;
    }


    private void Collapse(LinearLayout linLayout, final String text) {
        LinearLayout.LayoutParams layoutParamsLL = new LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        layoutParamsLL.setMargins(0, 5, 0, 0);

        LinearLayout collapse = new LinearLayout(activity);
        collapse.setLayoutParams(layoutParamsLL);
        collapse.setVerticalGravity(16);
        collapse.setOrientation(LinearLayout.VERTICAL);

        final LinearLayout collapseSub = new LinearLayout(activity);
        collapseSub.setVerticalGravity(16);
        collapseSub.setPadding(0, 5, 0, 5);
        collapseSub.setOrientation(LinearLayout.VERTICAL);
        collapseSub.setBackgroundColor(Color.parseColor("#222D38"));
        collapseSub.setVisibility(View.GONE);
        mCollapse = collapseSub;

        final TextView textView = new TextView(activity);
        textView.setBackgroundColor(CategoryBG);
        textView.setText("▽ " + text + " ▽");
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(TEXT_COLOR_2);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setPadding(0, 20, 0, 20);
        textView.setOnClickListener(new View.OnClickListener() {
            boolean isChecked;

            @Override
            public void onClick(View v) {

                boolean z = !this.isChecked;
                this.isChecked = z;
                if (z) {
                    collapseSub.setVisibility(View.VISIBLE);
                    textView.setText("△ " + text + " △");
                    return;
                }
                collapseSub.setVisibility(View.GONE);
                textView.setText("▽ " + text + " ▽");
            }
        });
        collapse.addView(textView);
        collapse.addView(collapseSub);
        linLayout.addView(collapse);
    }

    private View Category(String text) {
        TextView textView = new TextView(activity);
        textView.setBackgroundColor(CategoryBG);
        textView.setText(Html.fromHtml(text));
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(TEXT_COLOR_2);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setPadding(0, 5, 0, 5);
        return textView;
    }

    private View RichTextView(String text) {
        TextView textView = new TextView(activity);
        textView.setText(Html.fromHtml(text));
        textView.setTextColor(TEXT_COLOR_2);
        textView.setPadding(10, 5, 10, 5);
        return textView;
    }

    private View RichWebView(String text) {
        WebView wView = new WebView(activity);
        wView.loadData(text, "text/html", "utf-8");
        wView.setBackgroundColor(0x00000000); //Transparent
        wView.setPadding(0, 5, 0, 5);
        return wView;
    }

    private class EditTextString {
        private String text;

        public void setString(String s) {
            text = s;
        }

        public String getString() {
            return text;
        }
    }

    private class EditTextNum {
        private int val;

        public void setNum(int i) {
            val = i;
        }

        public int getNum() {
            return val;
        }
    }


    private boolean isViewCollapsed() {
        return rootFrame == null || mCollapsed.getVisibility() == View.VISIBLE;
    }

    private int convertDipToPixels(int i) {
        return (int) ((((float) i) * activity.getResources().getDisplayMetrics().density) + 0.5f);
    }

    @SuppressLint("WrongConstant")
    private int dp(int i) {
        return (int) TypedValue.applyDimension(1, (float) i, activity.getResources().getDisplayMetrics());
    }
    private boolean isNotInGame() {
        RunningAppProcessInfo runningAppProcessInfo = new RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(runningAppProcessInfo);
        return runningAppProcessInfo.importance != 100;
    }

    private void Thread() {
        if (rootFrame == null) {
            return;
        }
        if (isNotInGame()) {
            rootFrame.setVisibility(View.INVISIBLE);
        } else {
            rootFrame.setVisibility(View.VISIBLE);
        }
    }
}
