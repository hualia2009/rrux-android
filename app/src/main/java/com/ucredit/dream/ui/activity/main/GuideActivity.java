package com.ucredit.dream.ui.activity.main;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.custom.widgt.jazzyviewpager.OutlineContainer;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.ucredit.dream.R;
import com.ucredit.dream.ui.activity.account.LoginActivity;

public class GuideActivity extends BaseActivity implements OnPageChangeListener {

    @ViewInject(R.id.guide_view_pager)
    private ViewPager viewPager;
    private List<View> viewPageList;
//    private int current = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        this.initViewPager();
    }

    private void initViewPager() {
        this.viewPager.setPageMargin(0);

        this.viewPageList = new ArrayList<View>();
        this.viewPageList.add(this.getPageItem(0));
        this.viewPageList.add(this.getPageItem(1));
        this.viewPageList.add(this.getPageItem(2));
//        this.viewPageList.add(this.getPageItem(3));

        this.viewPager.setAdapter(new PagerAdapter() {

            @Override
            public boolean isViewFromObject(View view, Object object) {
                if (view instanceof OutlineContainer) {
                    return ((OutlineContainer) view).getChildAt(0) == object;
                }
                return view == object;
            }

            @Override
            public int getCount() {
                return GuideActivity.this.viewPageList.size();
            }

            @Override
            public void destroyItem(ViewGroup container, int position,
                    Object object) {
                container.removeView(GuideActivity.this.viewPageList
                    .get(position));
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                container.addView(GuideActivity.this.viewPageList.get(position));
                return GuideActivity.this.viewPageList.get(position);
            }

        });
        this.viewPager.setOnPageChangeListener(this);
        this.viewPager.postDelayed(new Runnable() {

            @Override
            public void run() {
                GuideActivity.this.anim(0);
            }
        }, 500);
    }

    private View getPageItem(int pageNum) {
        View view = this.getLayoutInflater().inflate(R.layout.guide_item, null);
        ViewUtils.inject(view);
//        LinearLayout layout = (LinearLayout) view.findViewById(R.id.root);
//        ImageView textBig = (ImageView) view.findViewById(R.id.big);
//        ImageView textSmall = (ImageView) view.findViewById(R.id.small);
//        ImageView icon = (ImageView) view.findViewById(R.id.icon);
//        ImageView dot = (ImageView) view.findViewById(R.id.dot);
        ImageView guideImage = (ImageView) view.findViewById(R.id.guide_image);
        ImageView enter = (ImageView) view.findViewById(R.id.guide_enter);
        switch (pageNum) {
            case 0:
//                layout.setBackgroundColor(this.getResources().getColor(
//                    R.color.page1));
//                textBig.setImageResource(R.drawable.guide_textbig1);
//                textSmall.setImageResource(R.drawable.guide_textsmall1);
//                icon.setImageResource(R.drawable.guide_icon1);
//                dot.setImageResource(R.drawable.guide_dot1);
                guideImage.setImageResource(R.drawable.guide_image1);
                break;
            case 1:
//                layout.setBackgroundColor(this.getResources().getColor(
//                    R.color.page2));
//                textBig.setImageResource(R.drawable.guide_textbig2);
//                textSmall.setImageResource(R.drawable.guide_textsmall2);
//                icon.setImageResource(R.drawable.guide_icon2);
//                dot.setImageResource(R.drawable.guide_dot2);
                guideImage.setImageResource(R.drawable.guide_image2);
                break;
//            case 2:
//                layout.setBackgroundColor(this.getResources().getColor(
//                    R.color.page3));
//                textBig.setImageResource(R.drawable.guide_textbig3);
//                textSmall.setImageResource(R.drawable.guide_textsmall3);
//                icon.setImageResource(R.drawable.guide_icon3);
//                dot.setImageResource(R.drawable.guide_dot3);
//                break;
            case 2:
//                layout.setBackgroundColor(this.getResources().getColor(
//                    R.color.page4));
//                textBig.setImageResource(R.drawable.guide_textbig4);
//                textSmall.setImageResource(R.drawable.guide_textsmall4);
//                icon.setImageResource(R.drawable.guide_icon4);
//                dot.setImageResource(R.drawable.guide_dot4);
                guideImage.setImageResource(R.drawable.guide_image3);
                enter.setVisibility(View.VISIBLE);
                enter.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        Intent intent = new Intent(GuideActivity.this,
                            LoginActivity.class);
                        GuideActivity.this.startActivity(intent);
                        GuideActivity.this.finish();
                    }
                });
                break;
            default:
                break;
        }
        return view;
    }

    private void hide(int position) {
//        if (this.viewPageList == null || this.viewPageList.size() == 0) {
//            return;
//        }
//        View view = this.viewPageList.get(position);
//        final ImageView textBig = (ImageView) view.findViewById(R.id.big);
//        final ImageView textSmall = (ImageView) view.findViewById(R.id.small);
//        final ImageView icon = (ImageView) view.findViewById(R.id.icon);
//        textBig.clearAnimation();
//        textSmall.clearAnimation();
//        icon.clearAnimation();
//        textBig.setVisibility(View.INVISIBLE);
//        textSmall.setVisibility(View.INVISIBLE);
//        icon.setVisibility(View.INVISIBLE);

    }

    private void anim(final int position) {
//        if (this.viewPageList == null || this.viewPageList.size() == 0) {
//            return;
//        }
//        View view = this.viewPageList.get(position);
//        final ImageView textBig = (ImageView) view.findViewById(R.id.big);
//        final ImageView textSmall = (ImageView) view.findViewById(R.id.small);
//        final ImageView icon = (ImageView) view.findViewById(R.id.icon);
//        TranslateAnimation animationBigRight = (TranslateAnimation) AnimationUtils
//            .loadAnimation(this, R.anim.guide_animbig_right);
//        TranslateAnimation animationBigLeft = (TranslateAnimation) AnimationUtils
//            .loadAnimation(this, R.anim.guide_animbig_left);
//        Animation animationIcon = AnimationUtils.loadAnimation(this,
//            R.anim.guide_icon);
//        final TranslateAnimation animationSmallRight = (TranslateAnimation) AnimationUtils
//            .loadAnimation(this, R.anim.guide_animbig_right);
//        final TranslateAnimation animationSmallLeft = (TranslateAnimation) AnimationUtils
//            .loadAnimation(this, R.anim.guide_animbig_left);
//        if (position >= this.current) {
//            animationBigRight.setAnimationListener(new AnimationListener() {
//
//                @Override
//                public void onAnimationStart(Animation arg0) {
//                    textBig.setVisibility(View.VISIBLE);
//                }
//
//                @Override
//                public void onAnimationRepeat(Animation arg0) {
//
//                }
//
//                @Override
//                public void onAnimationEnd(Animation arg0) {
//                    if (GuideActivity.this.current == position) {
//
//                        textSmall.startAnimation(animationSmallRight);
//                    }
//                }
//            });
//            animationSmallRight.setAnimationListener(new AnimationListener() {
//
//                @Override
//                public void onAnimationStart(Animation arg0) {
//                    textSmall.setVisibility(View.VISIBLE);
//                }
//
//                @Override
//                public void onAnimationRepeat(Animation arg0) {
//
//                }
//
//                @Override
//                public void onAnimationEnd(Animation arg0) {
//
//                }
//            });
//            textBig.startAnimation(animationBigRight);
//        } else {
//            animationBigLeft.setAnimationListener(new AnimationListener() {
//
//                @Override
//                public void onAnimationStart(Animation arg0) {
//                    textBig.setVisibility(View.VISIBLE);
//                }
//
//                @Override
//                public void onAnimationRepeat(Animation arg0) {
//
//                }
//
//                @Override
//                public void onAnimationEnd(Animation arg0) {
//                    if (GuideActivity.this.current == position) {
//
//                        textSmall.startAnimation(animationSmallLeft);
//                    }
//                }
//            });
//            animationSmallLeft.setAnimationListener(new AnimationListener() {
//
//                @Override
//                public void onAnimationStart(Animation arg0) {
//                    textSmall.setVisibility(View.VISIBLE);
//                }
//
//                @Override
//                public void onAnimationRepeat(Animation arg0) {
//
//                }
//
//                @Override
//                public void onAnimationEnd(Animation arg0) {
//
//                }
//            });
//            textBig.startAnimation(animationBigLeft);
//        }
//
//        animationIcon.setAnimationListener(new AnimationListener() {
//
//            @Override
//            public void onAnimationStart(Animation arg0) {
//                icon.setVisibility(View.VISIBLE);
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation arg0) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animation arg0) {
//
//            }
//        });
//        icon.startAnimation(animationIcon);
    }

    @Override
    protected boolean hasTitle() {
        return false;
    }

    @Override
    protected CharSequence getPublicTitle() {
        return "";
    }

    @Override
    protected int getContentId() {
        return R.layout.guide;
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {

    }

    @Override
    public void onPageSelected(int arg0) {
        this.anim(arg0);
        switch (arg0) {
            case 0:
                this.hide(1);
                this.hide(2);
                this.hide(3);
                break;
            case 1:
                this.hide(0);
                this.hide(2);
                this.hide(3);
                break;
            case 2:
                this.hide(0);
                this.hide(1);
                this.hide(3);
                break;
            case 3:
                this.hide(0);
                this.hide(1);
                this.hide(2);
                break;
            default:
                break;
        }
//        this.current = arg0;
    }

}
