/**
 * Martin Guillon
 * Copyright (c) 2009-2012 by Akylas. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 */
package akylas.slidemenu;

import java.lang.ref.WeakReference;

import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiActivityWindow;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiBaseActivity;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.TiContext;
import org.appcelerator.titanium.TiWindowManager;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.proxy.TiWindowProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.TiUIView;

import ti.modules.titanium.ui.WindowProxy;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import android.app.Activity;
import android.os.Message;

@Kroll.proxy(creatableInModule=AkylasSlidemenuModule.class, propertyAccessors={
	TiC.PROPERTY_STYLE,
	AkylasSlidemenuModule.PROPERTY_LEFT_VIEW,
	AkylasSlidemenuModule.PROPERTY_CENTER_VIEW,
	AkylasSlidemenuModule.PROPERTY_RIGHT_VIEW,
	AkylasSlidemenuModule.PROPERTY_PANNING_MODE,
	AkylasSlidemenuModule.PROPERTY_LEFT_VIEW_WIDTH,
	AkylasSlidemenuModule.PROPERTY_RIGHT_VIEW_WIDTH,
	AkylasSlidemenuModule.PROPERTY_FADING,
	AkylasSlidemenuModule.PROPERTY_TRANSITION_LEFT,
	AkylasSlidemenuModule.PROPERTY_TRANSITION_RIGHT,
	AkylasSlidemenuModule.PROPERTY_LEFT_VIEW_DISPLACEMENT,
	AkylasSlidemenuModule.PROPERTY_RIGHT_VIEW_DISPLACEMENT,
	TiC.PROPERTY_SHADOW_WIDTH
})
public class SlideMenuProxy extends WindowProxy implements TiActivityWindow, TiWindowManager
{
	private static final String TAG = "SlideMenuProxy";

	private static final int MSG_FIRST_ID = TiWindowProxy.MSG_LAST_ID + 1;

	private static final int MSG_TOGGLE_LEFT_VIEW = MSG_FIRST_ID + 100;
	private static final int MSG_TOGGLE_RIGHT_VIEW = MSG_FIRST_ID + 101;
	private static final int MSG_OPEN_LEFT_VIEW = MSG_FIRST_ID + 102;
	private static final int MSG_OPEN_RIGHT_VIEW = MSG_FIRST_ID + 103;
	private static final int MSG_CLOSE_LEFT_VIEW = MSG_FIRST_ID + 104;
	private static final int MSG_CLOSE_RIGHT_VIEW = MSG_FIRST_ID + 105;
	private static final int MSG_CLOSE_VIEWS = MSG_FIRST_ID + 106;

	protected static final int MSG_LAST_ID = MSG_FIRST_ID + 999;

	private WeakReference<SlidingMenu> slidingMenu;

	public SlideMenuProxy()
	{
		super();
        defaultValues.put(AkylasSlidemenuModule.PROPERTY_PANNING_MODE, AkylasSlidemenuModule.MENU_PANNING_CENTER_VIEW);
	}

	public SlideMenuProxy(TiContext tiContext)
	{
		this();
	}

	@Override
	public boolean handleMessage(Message msg)
	{
		switch (msg.what) {
			case MSG_TOGGLE_LEFT_VIEW: {
				handleToggleLeftView((Boolean)msg.obj);
				return true;
			}
			case MSG_TOGGLE_RIGHT_VIEW: {
				handleToggleRightView((Boolean)msg.obj);
				return true;
			}
			case MSG_OPEN_LEFT_VIEW: {
				handleOpenLeftView((Boolean)msg.obj);
				return true;
			}
			case MSG_OPEN_RIGHT_VIEW: {
				handleOpenRightView((Boolean)msg.obj);
				return true;
			}
			case MSG_CLOSE_LEFT_VIEW: {
				handleCloseLeftView((Boolean)msg.obj);
				return true;
			}
			case MSG_CLOSE_RIGHT_VIEW: {
				handleCloseRightView((Boolean)msg.obj);
				return true;
			}
			case MSG_CLOSE_VIEWS: {
				handleCloseViews((Boolean)msg.obj);
				return true;
			}
			default : {
				return super.handleMessage(msg);
			}
		}
	}

	@Override
	public TiUIView createView(Activity activity)
	{
		TiUISlideMenu v = new TiUISlideMenu(this, (TiBaseActivity) activity);
		slidingMenu = new WeakReference<SlidingMenu>((v).getSlidingMenu());
		return v;
	}
	
//	@Override
//    public void setActivity(Activity activity)
//    {
//        TiBaseActivity oldActivity = (TiBaseActivity) getActivity();
//        TiBaseActivity newActivity = (TiBaseActivity) activity;
//        Log.d(TAG, "setActivity :" + newActivity);
//        if (newActivity == oldActivity) return;
//        super.setActivity(activity);
//        
//        if (oldActivity != null) {
//            oldActivity.removeInterceptOnHomePressedEventListener(this);
//        }
//        
//        if (newActivity != null) {
//            newActivity.addInterceptOnHomePressedEventListener(this);
//        }
//    }

	
	@Override
    public void onWindowActivityCreated()
    {
        super.onWindowActivityCreated();
        ((TiUISlideMenu)peekView()).onWindowActivityCreated();
    }

	@Override
	public void releaseViews(boolean activityFinishing)
	{
		super.releaseViews(activityFinishing);
		TiViewProxy viewProxy = (TiViewProxy) getProperty(AkylasSlidemenuModule.PROPERTY_LEFT_VIEW);
		if (viewProxy != null)
		{
			viewProxy.releaseViews(activityFinishing);
		}
		viewProxy = (TiViewProxy) getProperty(AkylasSlidemenuModule.PROPERTY_RIGHT_VIEW);
		if (viewProxy != null)
		{
			viewProxy.releaseViews(activityFinishing);
		}
		viewProxy = (TiViewProxy) getProperty(AkylasSlidemenuModule.PROPERTY_CENTER_VIEW);
		if (viewProxy != null)
		{
			viewProxy.releaseViews(activityFinishing);
		}
	}
	
    @Override
	public void onWindowFocusChange(boolean focused) {
        TiViewProxy proxy = getCenterView();
        if (proxy != null && proxy instanceof TiWindowProxy)
        {
            ((TiWindowProxy) proxy).onWindowFocusChange(focused);
        }
        else super.onWindowFocusChange(focused);
    }
    
//    @Override
//    protected void addToDecorView() {
//        int style = getProperties().optInt(TiC.PROPERTY_STYLE, SlidingMenu.SLIDING_CONTENT);
//        if (style == SlidingMenu.SLIDING_CONTENT) {
//            super.addToDecorView();
//        }
//    }

	private void handleToggleLeftView(boolean animated)
	{
        blur();
		SlidingMenu menu = slidingMenu.get();
		menu.toggle(animated);
	}
	
	private void handleToggleRightView(boolean animated)
	{
        blur();
		SlidingMenu menu = slidingMenu.get();
		menu.toggleSecondary(animated);
	}
	
	private void handleOpenLeftView(boolean animated)
	{
        blur();
		SlidingMenu menu = slidingMenu.get();
		menu.showMenu(animated);
	}
	
	private void handleOpenRightView(boolean animated)
	{
        blur();
		SlidingMenu menu = slidingMenu.get();
		menu.showSecondaryMenu(animated);
	}
	
	private void handleCloseLeftView(boolean animated)
	{
        blur();
		SlidingMenu menu = slidingMenu.get();
		if (menu.isMenuShowing())
			menu.showContent(animated);
	}
	
	private void handleCloseRightView(boolean animated)
	{
        blur();
		SlidingMenu menu = slidingMenu.get();
		if (menu.isSecondaryMenuShowing())
			menu.showContent(animated);
	}
	
	private void handleCloseViews(boolean animated)
	{
        blur();
		SlidingMenu menu = slidingMenu.get();
		if (menu.isMenuShowing() || menu.isSecondaryMenuShowing())
			menu.showContent(animated);
	}
	
	
	@Kroll.method
	public void toggleLeftView(@Kroll.argument(optional = true) Object obj)
	{
		Boolean animated = true;
		if (obj != null) {
			animated = TiConvert.toBoolean(obj);
		}
		
		if (TiApplication.isUIThread()) {
			handleToggleLeftView(animated);
			return;
		}
		Message message = getMainHandler().obtainMessage(MSG_TOGGLE_LEFT_VIEW, animated);
		message.sendToTarget();
	}
	
	@Kroll.method
	public void toggleRightView(@Kroll.argument(optional = true) Object obj)
	{
		Boolean animated = true;
		if (obj != null) {
			animated = TiConvert.toBoolean(obj);
		}
		
		if (TiApplication.isUIThread()) {
			handleToggleRightView(animated);
			return;
		}
		Message message = getMainHandler().obtainMessage(MSG_TOGGLE_RIGHT_VIEW, animated);
		message.sendToTarget();
	}
	
	@Kroll.method
	public void openLeftView(@Kroll.argument(optional = true) Object obj)
	{
		Boolean animated = true;
		if (obj != null) {
			animated = TiConvert.toBoolean(obj);
		}
		
		if (TiApplication.isUIThread()) {
			handleOpenLeftView(animated);
			return;
		}
		Message message = getMainHandler().obtainMessage(MSG_OPEN_LEFT_VIEW, animated);
		message.sendToTarget();
	}
	
	@Kroll.method
	public void openRightView(@Kroll.argument(optional = true) Object obj)
	{
		Boolean animated = true;
		if (obj != null) {
			animated = TiConvert.toBoolean(obj);
		}
		
		if (TiApplication.isUIThread()) {
			handleOpenRightView(animated);
			return;
		}
		Message message = getMainHandler().obtainMessage(MSG_OPEN_RIGHT_VIEW, animated);
		message.sendToTarget();
	}
	
	@Kroll.method
	public void closeLeftView(@Kroll.argument(optional = true) Object obj)
	{
		Boolean animated = true;
		if (obj != null) {
			animated = TiConvert.toBoolean(obj);
		}
		
		if (TiApplication.isUIThread()) {
			handleCloseLeftView(animated);
			return;
		}
		Message message = getMainHandler().obtainMessage(MSG_CLOSE_LEFT_VIEW, animated);
		message.sendToTarget();
	}
	
	@Kroll.method
	public void closeRightView(@Kroll.argument(optional = true) Object obj)
	{
		Boolean animated = true;
		if (obj != null) {
			animated = TiConvert.toBoolean(obj);
		}
		
		if (TiApplication.isUIThread()) {
			handleCloseRightView(animated);
			return;
		}
		Message message = getMainHandler().obtainMessage(MSG_CLOSE_RIGHT_VIEW, animated);
		message.sendToTarget();
	}
	
	@Kroll.method
	public void closeViews(@Kroll.argument(optional = true) Object obj)
	{
		Boolean animated = true;
		if (obj != null) {
			animated = TiConvert.toBoolean(obj);
		}
		
		if (TiApplication.isUIThread()) {
			handleCloseViews(animated);
			return;
		}
		Message message = getMainHandler().obtainMessage(MSG_CLOSE_VIEWS, animated);
		message.sendToTarget();
	}
	
	@Kroll.method
	public boolean isLeftViewOpened()
	{
		SlidingMenu menu = slidingMenu.get();
		return menu.isMenuShowing();
	}
	
	@Kroll.method
	public boolean isRightViewOpened()
	{
		SlidingMenu menu = slidingMenu.get();
		return menu.isSecondaryMenuShowing();
	}
	
	@Kroll.method
	public int getRealLeftViewWidth()
	{
		SlidingMenu menu = slidingMenu.get();
		return menu.getBehindWidth();
	}
	
	@Kroll.method
	public int getRealRightViewWidth()
	{
		SlidingMenu menu = slidingMenu.get();
		return menu.getBehindWidth();
	}

	@Override
	public boolean handleClose(TiWindowProxy proxy, Object arg) {
		return false;
	}

	@Override
	public boolean handleOpen(TiWindowProxy proxy, Object arg) {
		return false;
	}
	
	private TiViewProxy getCenterView() {
	    if (hasProperty(AkylasSlidemenuModule.PROPERTY_CENTER_VIEW))
        {
          return (TiViewProxy)getProperty(AkylasSlidemenuModule.PROPERTY_CENTER_VIEW);
        }
	    return null;
	}

	@Override
	public boolean realUpdateOrientationModes()
	{
	    TiViewProxy proxy = getCenterView();
        if (proxy != null && proxy instanceof TiWindowProxy)
        {
            if (((TiWindowProxy) proxy).realUpdateOrientationModes())
                return true;
        }
		return super.realUpdateOrientationModes();
	}

	@Override
	public KrollProxy getParentForBubbling(TiWindowProxy proxy) {
		return this;
	}

//    @Override
//    public boolean interceptOnHomePressed() {
//        blur();
//        SlidingMenu menu = slidingMenu.get();
//        menu.toggle(true);
//        return true;
//    }
}
