package com.ladjzero.uzlee.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.ladjzero.hipda.Forum;
import com.ladjzero.hipda.Post;
import com.ladjzero.hipda.Posts;
import com.ladjzero.hipda.User;
import com.ladjzero.uzlee.R;
import com.nineoldandroids.animation.Animator;
import com.orhanobut.logger.Logger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by ladjzero on 2015/2/28.
 */
public class Utils {

    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public static String getFirstChar(String input) {
        if (input.length() > 0) {
            String first = input.substring(0, 1);
            char f = first.charAt(0);

            if ('a' <= f && f <= 'z') {
                first = first.toUpperCase();
            }

            return first;
        } else {
            return "";
        }
    }

    public static int changeColorSaturability(int color, float s) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        float newS = hsv[1] * s;
        hsv[1] = newS;

        return Color.HSVToColor(hsv);
    }

    public static int getColor(Context context, int resId) {
        return context.getResources().getColor(resId);
    }

    public static String toHtml(Posts posts) {
        Logger.d(JSON.toJSONString(posts));

        return StringUtils.join(CollectionUtils.collect(posts, new Transformer() {
            @Override
            public Object transform(Object o) {
                Post post = (Post) o;
                User user = post.getAuthor();

                return "<img src=\"" + user.getImage() + "\" onclick=\"ActivityPosts.onUserClick(2)\"><h3>" + user.getName() + "</h3>" + JSON.toJSONString(post);
            }
        }), "");
    }

    public static String readAssetFile(Context context, String file) {
        BufferedReader reader = null;
        StringBuilder ret = new StringBuilder();

        try {
            reader = new BufferedReader(new InputStreamReader(context.getAssets().open(file), "UTF-8"));

            String mLine = reader.readLine();

            while (mLine != null) {
                ret.append(mLine);
                mLine = reader.readLine();
            }

            return ret.toString();
        } catch (IOException e) {
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static void fadeOut(final View view) {
        YoYo.with(Techniques.FadeOut).duration(100).withListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).playOn(view);
    }

    public static void fadeIn(final View view) {
        YoYo.with(Techniques.FadeIn).duration(100).withListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        }).playOn(view);
    }

    public static int parseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return 0;
        }
    }

    public static String prettyTime(String timeStr) {
        Date mNow = new Date();

        try {
            Date thatDate = dateFormat.parse(timeStr);

            if (DateUtils.isSameDay(thatDate, mNow)) {
                return DateFormatUtils.format(thatDate, "HH:mm");
            } else if (DateUtils.isSameDay(DateUtils.addDays(thatDate, 1), mNow)) {
                return DateFormatUtils.format(thatDate, "昨天 HH:mm");
            } else if (mNow.getYear() == thatDate.getYear()) {
                return DateFormatUtils.format(thatDate, "M月d日");
            } else {
                return DateFormatUtils.format(thatDate, "yyyy年M月d日");
            }
        } catch (ParseException e) {
            return timeStr;
        }
    }

    public static void openInBrowser(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        context.startActivity(intent);
    }

    public static int getTheme(String color) {
        if ("red".equals(color)) return R.style.AppBaseTheme_Day_Red;
        if ("carrot".equals(color)) return R.style.AppBaseTheme_Day_Carrot;
        if ("orange".equals(color)) return R.style.AppBaseTheme_Day_Orange;
        if ("green".equals(color)) return R.style.AppBaseTheme_Day_Green;
        if ("blueGrey".equals(color)) return R.style.AppBaseTheme_Day_BlueGrey;
        if ("blue".equals(color)) return R.style.AppBaseTheme_Day_Blue;
        if ("dark".equals(color)) return R.style.AppBaseTheme_Day_Dark;
        if ("night".equals(color)) return R.style.AppBaseTheme_Night;
        return R.style.AppBaseTheme_Day_Purple;
    }

    public static String getThemeName(Context context, String color) {
        Resources res = context.getResources();
        if ("red".equals(color)) return res.getString(R.string.red);
        if ("carrot".equals(color)) return res.getString(R.string.carrot);
        if ("orange".equals(color)) return res.getString(R.string.orange);
        if ("green".equals(color)) return res.getString(R.string.green);
        if ("blueGrey".equals(color)) return res.getString(R.string.blueGrey);
        if ("blue".equals(color)) return res.getString(R.string.blue);
        if ("dark".equals(color)) return res.getString(R.string.dark);
        if ("night".equals(color)) return res.getString(R.string.night);
        return res.getString(R.string.purple);
    }

    public static String getFontSizeName(String fontsize) {
        if ("big".equals(fontsize)) return "更大";
        if ("bigger".equals(fontsize)) return  "比更大还更大";
        return "正常";
    }

    public static String getSortName(String sort) {
        if ("2".equals(sort)) return "回复时间";
        return "发表时间";
    }

    public static int getThemeColor(Context context, int attrId) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(attrId, typedValue, true);
        return typedValue.data;
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void replaceActivity(Activity current, Class next) {
        gotoActivity(current, next, Intent.FLAG_ACTIVITY_TASK_ON_HOME | Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    public static void gotoActivity(Activity current, Class next) {
        Intent intent = new Intent(current, next);
        current.startActivity(intent);
        current.finish();
    }

    public static void gotoActivity(Activity current, Class next, int flags) {
        Intent intent = new Intent(current, next);
        intent.setFlags(flags);
        current.startActivity(intent);
        current.finish();
    }

    public static int dp2px(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public static abstract class OnAnimatorStartEndListener implements Animator.AnimatorListener {

        @Override
        public abstract void onAnimationStart(Animator animation);

        @Override
        public abstract void onAnimationEnd(Animator animation);

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }

    public static List<Forum> getForums(Context context) {
        String json = Utils.readAssetFile(context, "hipda.json");
        List<Forum> forums = JSON.parseArray(json, Forum.class);
        addALLType(forums);

        return forums;
    }

    private static void addALLType(List<Forum> forums) {
        Forum.Type all = new Forum.Type();
        all.setId(-1);
        all.setName("全部");

        for (Forum f : forums) {
            List<Forum.Type> types = f.getTypes();
            List<Forum> children = f.getChildren();

            if (types != null) types.add(0, all);
            if (children != null) addALLType(children);
        }
    }
}
