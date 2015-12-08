package akylas.slidemenu;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiBaseActivity;
import org.appcelerator.titanium.TiBaseActivity.ConfigurationChangedListener;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiDimension;
import org.appcelerator.titanium.TiWindowManager;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.proxy.TiWindowProxy;
import org.appcelerator.titanium.transition.Transition;
import org.appcelerator.titanium.transition.TransitionHelper;
import org.appcelerator.titanium.transition.TransitionHelper.SubTypes;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.util.TiUIHelper;
import org.appcelerator.titanium.view.TiCompositeLayout;
import org.appcelerator.titanium.view.TiUIView;

import ti.modules.titanium.ui.widget.TiUIScrollableView.TiViewPagerLayout;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.ViewTransformer;

public class TiUISlideMenu extends TiUIView implements ConfigurationChangedListener{
	private SlidingMenu slidingMenu;
	private TiViewProxy leftView;
	private TiViewProxy rightView;
	private TiViewProxy centerView;
	private static final String TAG = "TiUISlideMenu";
	private TiBaseActivity activity;
	private TiDimension menuWidth = defaultWidth;
	private int realMenuWidth;
	private TiDimension rightMenuWidth = defaultWidth;
	private int realRightMenuWidth;
	private static TiDimension defaultDisplacement = new TiDimension(0, TiDimension.TYPE_WIDTH);
	private static TiDimension defaultWidth = new TiDimension(200, TiDimension.TYPE_WIDTH);
	private TiDimension leftViewDisplacement =  defaultDisplacement;
	private TiDimension rightViewDisplacement =  defaultDisplacement;
	private TiCompositeLayout parentViewForChildren;
	private int style = SlidingMenu.SLIDING_CONTENT;
//	private MaterialMenuIconToolbar materialMenu;
    protected static final int TIFLAG_NEEDS_MENU               = 0x00000001;
    protected static final int TIFLAG_NEEDS_MENU_WIDTH         = 0x00000002;
    protected static final int TIFLAG_NEEDS_MENU_DISPLACEMENT  = 0x00000003;
	
	public TiUISlideMenu(final SlideMenuProxy proxy, TiBaseActivity activity)
	{
		super((TiViewProxy)proxy);
		this.activity = activity;
		activity.addConfigurationChangedListener(this);
        // configure the SlidingMenu
//		materialMenu = new MaterialMenuIconToolbar(activity, Color.WHITE, Stroke.THIN) {
//	        @Override public int getToolbarViewId() {
//	            Context context = TiUISlideMenu.this.activity;
//	            return context.getResources().getIdentifier("toolbar", "id", context.getPackageName());
//	        }
//	    };
//	    materialMenu.setNeverDrawTouch(true);
		slidingMenu = new SlidingMenu(activity) {
			@Override
			protected void onSizeChanged(int w, int h, int oldw, int oldh) {
				super.onSizeChanged(w, h, oldw, oldh);
				updateMenuWidth();
			}
			@Override
			protected void onLayout(boolean changed, int left, int top, int right, int bottom)
			{
				super.onLayout(changed, left, top, right, bottom);
				if (changed) {
                    TiUIHelper.firePostLayoutEvent(TiUISlideMenu.this);
                }
			}
			
			@Override
			public void attachViewToParent(ViewGroup group) {
				super.attachViewToParent(group);
				parentViewForChildren = new TiCompositeLayout(this.getContext());
				RelativeLayout layout = new RelativeLayout(this.getContext());
				layout.addView(parentViewForChildren, new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
				group.addView(layout, new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			}
		};
		slidingMenu.setActionBarOverlay(TiConvert.toBoolean(proxy.getProperty(TiC.PROPERTY_ACTIONBAR_OVERLAY), false));
//		slidingMenu.attachToActivity(activity, style, TiConvert.toBoolean(proxy.getProperty(TiC.PROPERTY_ACTIONBAR_OVERLAY), false));
		slidingMenu.setClassForNonViewPager(TiViewPagerLayout.class);
		slidingMenu.setOnCloseListener(new SlidingMenu.OnCloseListener() {
			@Override
			public void onClose(int leftOrRight, boolean animated, int duration) {
//                materialMenu.setState(MaterialMenuDrawable.IconState.BURGER);
				if (proxy.hasListeners(AkylasSlidemenuModule.EVENT_CLOSE_MENU, false))
				{
					KrollDict options = new KrollDict();
					options.put(AkylasSlidemenuModule.PROPERTY_SIDE, (leftOrRight == 1)?AkylasSlidemenuModule.RIGHT_VIEW:AkylasSlidemenuModule.LEFT_VIEW);
					options.put(TiC.PROPERTY_ANIMATED, animated);
					options.put(TiC.PROPERTY_DURATION, duration);
					proxy.fireEvent(AkylasSlidemenuModule.EVENT_CLOSE_MENU, options, false, false);
				}
			}
		});
		slidingMenu.setOnClosedListener(new SlidingMenu.OnClosedListener() {
            @Override
            public void onClosed() {
//                materialMenu.setState(MaterialMenuDrawable.IconState.BURGER);
                if (proxy.hasListeners(AkylasSlidemenuModule.EVENT_CLOSED_MENU, false))
                {
                    KrollDict options = new KrollDict();
                    proxy.fireEvent(AkylasSlidemenuModule.EVENT_CLOSED_MENU, options, false, false);
                }
            }
        });
		slidingMenu.setOnOpenListener(new SlidingMenu.OnOpenListener() {
			@Override
			public void onOpen(int leftOrRight, boolean animated, int duration) {
//			    materialMenu.setState(MaterialMenuDrawable.IconState.ARROW);
				if (proxy.hasListeners(AkylasSlidemenuModule.EVENT_OPEN_MENU, false))
				{
					KrollDict options = new KrollDict();
					options.put(AkylasSlidemenuModule.PROPERTY_SIDE, (leftOrRight == 1)?AkylasSlidemenuModule.RIGHT_VIEW:AkylasSlidemenuModule.LEFT_VIEW);
					options.put(TiC.PROPERTY_ANIMATED, animated);
					options.put(TiC.PROPERTY_DURATION, duration);
					proxy.fireEvent(AkylasSlidemenuModule.EVENT_OPEN_MENU, options, false, false);
				}
			}
		});
		slidingMenu.setOnScrolledListener(new SlidingMenu.OnScrolledListener() {
			
			@Override
			public void onScrolled(int position, float positionOffset,
	                int positionOffsetPixels) {
                updateOffset(positionOffsetPixels, 1);
			}

			@Override
			public void onScrolledEnded(int position, float positionOffset,
	                int positionOffsetPixels) {
                updateOffset(positionOffsetPixels, 2);
			}

			@Override
			public void onScrolledStarted(int position, float positionOffset,
	                int positionOffsetPixels) {			    
                updateOffset(positionOffsetPixels, 0);
			}
		});
		
		slidingMenu.setMode(SlidingMenu.LEFT);
		slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        slidingMenu.setTouchModeBehind(SlidingMenu.TOUCHMODE_NONE);
		slidingMenu.setFadeDegree(0.0f);
		slidingMenu.setBehindScrollScale(0.0f);
		slidingMenu.setShadowWidth(20);
		
		updateMenuWidth();
		
		style = proxy.getProperties().optInt(TiC.PROPERTY_STYLE, style);
		if (style == SlidingMenu.SLIDING_CONTENT) {
			parentViewForChildren = new TiCompositeLayout(activity);
			RelativeLayout layout = new RelativeLayout(activity);
			layout.addView(parentViewForChildren, new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			slidingMenu.addView(layout);
			slidingMenu.setContent(new TiCompositeLayout(activity));
		}
//		else {
//			slidingMenu.attachToActivity(activity, style);
//		}
		
		int[] colors1 = {Color.argb(0, 0, 0, 0), Color.argb(128, 0, 0, 0)};
		GradientDrawable shadow = new GradientDrawable(Orientation.LEFT_RIGHT, colors1);
		GradientDrawable shadowR = new GradientDrawable(Orientation.RIGHT_LEFT, colors1);
		slidingMenu.setShadowDrawable(shadow);
		slidingMenu.setSecondaryShadowDrawable(shadowR);

		setNativeView(slidingMenu);
	}
	
	
	private void updateOffset(final int positionOffsetPixels, int state) {
	    final String event = (state == 0)?TiC.EVENT_SCROLLSTART: ((state == 1)?TiC.EVENT_SCROLL:TiC.EVENT_SCROLLEND);
        if (proxy.hasListeners(event, false))
        {
            boolean rightMenu = positionOffsetPixels > 0;
            int offset = Math.abs(positionOffsetPixels);
            float percent = (float)offset / (rightMenu ? slidingMenu.getSecondaryBehindWidth(): slidingMenu.getBehindWidth());
            
            KrollDict options = new KrollDict();
            options.put(AkylasSlidemenuModule.PROPERTY_SIDE, rightMenu?AkylasSlidemenuModule.RIGHT_VIEW:AkylasSlidemenuModule.LEFT_VIEW);
            options.put("offset", -positionOffsetPixels);
            options.put("percent", percent);
            proxy.fireEvent(event, options, false, false);
        }
//        materialMenu.setTransformationOffset(
//                MaterialMenuDrawable.AnimationState.BURGER_ARROW,
//                percent
//            );
	}
	
	@Override
	public ViewGroup getParentViewForChild()
	{
		return parentViewForChildren;
	}
	
	public SlidingMenu getSlidingMenu()
	{
		return slidingMenu;
	}
	
	private void updateMenuWidth()
	{
		realMenuWidth = menuWidth.getAsPixels(slidingMenu);
		if (realMenuWidth > 0)
			slidingMenu.setBehindWidth(realMenuWidth);
		else
			slidingMenu.setBehindOffset(-realMenuWidth);
		
		realRightMenuWidth = rightMenuWidth.getAsPixels(slidingMenu);
		if (realRightMenuWidth > 0)
			slidingMenu.setSecondaryBehindWidth(realRightMenuWidth);
		else
			slidingMenu.setSecondaryBehindOffset(-realRightMenuWidth);
		updateDisplacements();
	}

	private void updateDisplacements()
	{
		int leftMenuWidth = realMenuWidth;
		if (leftMenuWidth < 0) {
			leftMenuWidth += slidingMenu.getWidth();
		}
		if (leftMenuWidth > 0) slidingMenu.setBehindScrollScale(leftViewDisplacement.getAsPixels(leftMenuWidth, leftMenuWidth)/(float)leftMenuWidth);

		int myRightMenuWidth = realRightMenuWidth;
		if (myRightMenuWidth < 0) {
			myRightMenuWidth += slidingMenu.getWidth();
		}
		if (myRightMenuWidth > 0) slidingMenu.setBehindSecondaryScrollScale(rightViewDisplacement.getAsPixels(myRightMenuWidth, myRightMenuWidth)/(float)myRightMenuWidth);
	}
	
	public int getLeftMenuWidth()
	{
		return realMenuWidth;
	}
	
	public int getRightMenuWidth()
	{
		return realMenuWidth;
	}
	
	// for animations
	private static Interpolator interp = new Interpolator() {
		@Override
		public float getInterpolation(float t) {
			t -= 1.0f;
			return t * t * t + 1.0f;
		}		
	};
		
	private void updateAnimationMode(final Transition transition, final boolean right)
	{
		ViewTransformer transformer = null;
		if (transition != null) {
			final SubTypes subtype = transition.subType;
			transformer = new ViewTransformer() {
				@Override
				public void transformView(View view, float percentOpen) {
					float realPercent;
					if (right) {
						realPercent = TransitionHelper.isPushSubType(subtype)?(1 - percentOpen):(percentOpen - 1);
					}
					else {
						realPercent = TransitionHelper.isPushSubType(subtype)?(percentOpen - 1):(1 - percentOpen);
					}
					transition.transformView(view, realPercent);
//					canvas.translate(0, canvas.getHeight()*(1-interp.getInterpolation(percentOpen)));
				}			
			};
		}
//		if (mode == AkylasSlidemenuModule.ANIMATION_SCALE) {
//			// scale
//			transformer = new CanvasTransformer() {
//				@Override
//				public void transformCanvas(Canvas canvas, float percentOpen) {
//					canvas.scale(percentOpen, 1, 0, 0);
//				}			
//			};	
//		} else if (mode == AkylasSlidemenuModule.ANIMATION_SLIDEUP) {
//			// slide
//			transformer = new ViewTransformer() {
//				@Override
//				public void transformCanvas(Canvas canvas, float percentOpen) {
//					canvas.translate(0, canvas.getHeight()*(1-interp.getInterpolation(percentOpen)));
//				}			
//			};
//		} else if (mode == AkylasSlidemenuModule.ANIMATION_ZOOM) {
//			// zoom animation
//			transformer = new CanvasTransformer() {
//				@Override
//				public void transformCanvas(Canvas canvas, float percentOpen) {
//					float scale = (float) (percentOpen*0.25 + 0.75);
//					canvas.scale(scale, scale, canvas.getWidth()/2, canvas.getHeight()/2);
//				}
//			};
//		}
		
		if (right)
			slidingMenu.setBehindSecondaryViewTransformer(transformer);
		else 
			slidingMenu.setBehindViewTransformer(transformer);
		// we need to reset the scrollScale when applying custom animations
		if( transformer != null){
			leftViewDisplacement = defaultDisplacement;
			rightViewDisplacement = defaultDisplacement;
			updateDisplacements();
		}

	}
	
	private void updatePanningMode(int panningMode)
	{
		slidingMenu.setTouchModeBehind(SlidingMenu.TOUCHMODE_MARGIN);
		if (panningMode == AkylasSlidemenuModule.MENU_PANNING_BORDERS) {
            slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
//            slidingMenu.setTouchModeBehind(SlidingMenu.TOUCHMODE_MARGIN);
		} else if (panningMode == AkylasSlidemenuModule.MENU_PANNING_CENTER_VIEW)
			slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		else if (panningMode == AkylasSlidemenuModule.MENU_PANNING_ALL_VIEWS) {
			slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
//			slidingMenu.setTouchModeBehind(SlidingMenu.TOUCHMODE_FULLSCREEN);
		} else{
			slidingMenu.setTouchModeBehind(SlidingMenu.TOUCHMODE_NONE);
			slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
		}
	}
	
	private void updateMenus() {
		View leftV = null;
		View rightV = null;
		if (this.leftView != null) {
			leftV = (this.leftView).getOrCreateView().getOuterView();
			TiUIHelper.removeViewFromSuperView(leftV);
		}
		if (this.rightView != null) {
			rightV = (this.rightView).getOrCreateView().getOuterView();
			TiUIHelper.removeViewFromSuperView(rightV);
		}


		if (leftV != null && rightV != null) {
			slidingMenu.setMode(SlidingMenu.LEFT_RIGHT);
			slidingMenu.setMenu(leftV);
			slidingMenu.setSecondaryMenu(rightV);
		}
		else if (rightV != null)
		{
			slidingMenu.setMode(SlidingMenu.RIGHT);
			slidingMenu.setMenu(rightV);
			slidingMenu.setSecondaryMenu(null);
		}
		else if (leftV != null)
		{
			slidingMenu.setMode(SlidingMenu.LEFT);
			slidingMenu.setMenu(leftV);
			slidingMenu.setSecondaryMenu(null);
		}
		else
		{
			slidingMenu.setMode(SlidingMenu.LEFT);
			slidingMenu.setMenu(null);
			slidingMenu.setSecondaryMenu(null);
		}
	}
	
	@Override
    public void propertySet(String key, Object newValue, Object oldValue,
            boolean changedProperty) {
        switch (key) {
        case AkylasSlidemenuModule.PROPERTY_TRANSITION_LEFT:
            updateAnimationMode(TransitionHelper.transitionFromObject(newValue, null, null), false);
            break;
        case AkylasSlidemenuModule.PROPERTY_TRANSITION_RIGHT:
            updateAnimationMode(TransitionHelper.transitionFromObject(newValue, null, null), true);
            break;
        case AkylasSlidemenuModule.PROPERTY_LEFT_VIEW:
            setProxy((TiViewProxy) newValue, 1);
            mProcessUpdateFlags |= TIFLAG_NEEDS_MENU;
            break;
        case AkylasSlidemenuModule.PROPERTY_RIGHT_VIEW:
            setProxy((TiViewProxy) newValue, 2);
            mProcessUpdateFlags |= TIFLAG_NEEDS_MENU;
            break;
        case AkylasSlidemenuModule.PROPERTY_CENTER_VIEW:
            setProxy((TiViewProxy) newValue, 0);
            if (changedProperty) {
                slidingMenu.showContent(true);
            }
            break;
        case AkylasSlidemenuModule.PROPERTY_PANNING_MODE:
            updatePanningMode(TiConvert.toInt(newValue, AkylasSlidemenuModule.MENU_PANNING_CENTER_VIEW));
            break;
        case AkylasSlidemenuModule.PROPERTY_LEFT_VIEW_WIDTH:
            menuWidth = TiConvert.toTiDimension(newValue, TiDimension.TYPE_WIDTH);
            mProcessUpdateFlags |= TIFLAG_NEEDS_MENU_WIDTH;
            break;
        case AkylasSlidemenuModule.PROPERTY_RIGHT_VIEW_WIDTH:
            rightMenuWidth = TiConvert.toTiDimension(newValue, TiDimension.TYPE_WIDTH);
            mProcessUpdateFlags |= TIFLAG_NEEDS_MENU_WIDTH;
            break;
        case AkylasSlidemenuModule.PROPERTY_FADING:
            slidingMenu.setFadeDegree(TiConvert.toFloat(newValue));
            break;
        case AkylasSlidemenuModule.PROPERTY_LEFT_VIEW_DISPLACEMENT:
            leftViewDisplacement = TiConvert.toTiDimension(newValue, TiDimension.TYPE_WIDTH);
            mProcessUpdateFlags |= TIFLAG_NEEDS_MENU_DISPLACEMENT;
            break;
        case AkylasSlidemenuModule.PROPERTY_RIGHT_VIEW_DISPLACEMENT:
            rightViewDisplacement = TiConvert.toTiDimension(newValue, TiDimension.TYPE_WIDTH);
            mProcessUpdateFlags |= TIFLAG_NEEDS_MENU_DISPLACEMENT;
            break;
        case TiC.PROPERTY_SHADOW_WIDTH:
            slidingMenu.setShadowWidth(TiConvert.toInt(newValue));
            break;
        case TiC.PROPERTY_ENABLED:
            slidingMenu.setSlidingEnabled(TiConvert.toBoolean(newValue));
            break;
        default:
            super.propertySet(key, newValue, oldValue, changedProperty);
            break;
        }
    }
    
    @Override
    protected void didProcessProperties() {
        super.didProcessProperties();
        if ((mProcessUpdateFlags & TIFLAG_NEEDS_MENU) != 0) {
            updateMenus();
            mProcessUpdateFlags &= ~TIFLAG_NEEDS_MENU;
        }
        if ((mProcessUpdateFlags & TIFLAG_NEEDS_MENU_WIDTH) != 0) {
            updateMenuWidth();
            mProcessUpdateFlags &= ~TIFLAG_NEEDS_MENU_WIDTH;
        }
        if ((mProcessUpdateFlags & TIFLAG_NEEDS_MENU_DISPLACEMENT) != 0) {
            updateDisplacements();
            mProcessUpdateFlags &= ~TIFLAG_NEEDS_MENU_DISPLACEMENT;
        }
    }

	@Override
	public void onConfigurationChanged(TiBaseActivity activity,
			Configuration newConfig) {
		updateMenuWidth();
	}
	
    public void onWindowActivityCreated()
    {
        if (style == SlidingMenu.SLIDING_WINDOW) {
            slidingMenu.attachToActivity(activity, style);
        }
    }
	
	private void setProxy(TiViewProxy newProxy, int forSlideView)//0center,1left,2right
	{
		
		boolean isCenterView = forSlideView == 0;
		TiViewProxy oldProxy = isCenterView?this.centerView:((forSlideView == 1)?this.leftView:this.rightView);
		if (newProxy == oldProxy) return;
		TiCompositeLayout content = ((TiCompositeLayout) ((style == SlidingMenu.SLIDING_CONTENT)?slidingMenu.getContent():activity.getLayout()));
		int index = 0;
		if (isCenterView && oldProxy != null)
		{
			index = content.indexOfChild(oldProxy.getOrCreateView().getNativeView());
		}
		if (newProxy != null && newProxy instanceof TiViewProxy) {
				TiBaseActivity activity = (TiBaseActivity) this.proxy.getActivity();
				newProxy.setActivity(activity);
				newProxy.setParent(this.proxy);
				if (isCenterView) {
				    if (newProxy instanceof TiWindowProxy) {
                        ((TiWindowProxy) newProxy).setWindowManager((TiWindowManager) this.proxy);
                        activity.setWindowProxy((TiWindowProxy) newProxy);
                    }
					TiCompositeLayout.LayoutParams params = new TiCompositeLayout.LayoutParams();
					params.autoFillsHeight = true;
					params.autoFillsWidth = true;
					content.addView(newProxy.getOrCreateView().getOuterView(), index, params);
				}
				if (newProxy instanceof TiWindowProxy) {
					((TiWindowProxy)newProxy).onWindowActivityCreated();
					newProxy.focus();
				}
		} else {
			Log.e(TAG, "Invalid type for centerView");
		}
		if (oldProxy != null)
		{
			if (isCenterView) {
				content.removeView(oldProxy.getOuterView());
				if (oldProxy instanceof TiWindowProxy) {
					((TiWindowProxy) oldProxy).setWindowManager(null);
				}
			}
			oldProxy.setParent(null);
			oldProxy.setActivity(null);
			oldProxy.blur();
		}
		switch (forSlideView) {
		case 0:
			this.centerView = newProxy;
			break;
		case 1:
			this.leftView = newProxy;
			break;
		case 2:
			this.rightView = newProxy;
			break;
		default:
			break;
		}
	}
}
