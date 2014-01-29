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
	AkylasSlidemenuModule.PROPERTY_LEFT_VIEW,
	AkylasSlidemenuModule.PROPERTY_CENTER_VIEW,
	AkylasSlidemenuModule.PROPERTY_RIGHT_VIEW,
	AkylasSlidemenuModule.PROPERTY_PANNING_MODE,
	AkylasSlidemenuModule.PROPERTY_LEFT_VIEW_WIDTH,
	AkylasSlidemenuModule.PROPERTY_RIGHT_VIEW_WIDTH,
	AkylasSlidemenuModule.PROPERTY_FADING,
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
//		slideMenuActivity = new WeakReference<Activity>(activity);
		TiUISlideMenu v = new TiUISlideMenu(this, (TiBaseActivity) activity);
		slidingMenu = new WeakReference<SlidingMenu>((v).getSlidingMenu());
		setView(v);
		return v;
	}

	@Override
	public void releaseViews()
	{
		super.releaseViews();
		if (hasProperty(AkylasSlidemenuModule.PROPERTY_LEFT_VIEW))
		{
			((TiViewProxy)getProperty(AkylasSlidemenuModule.PROPERTY_LEFT_VIEW)).releaseViews();
		}
		if (hasProperty(AkylasSlidemenuModule.PROPERTY_RIGHT_VIEW))
		{
			((TiViewProxy)getProperty(AkylasSlidemenuModule.PROPERTY_RIGHT_VIEW)).releaseViews();
		}
		if (hasProperty(AkylasSlidemenuModule.PROPERTY_CENTER_VIEW))
		{
			((TiViewProxy)getProperty(AkylasSlidemenuModule.PROPERTY_CENTER_VIEW)).releaseViews();
		}
	}

	private void handleToggleLeftView(boolean animated)
	{
		SlidingMenu menu = slidingMenu.get();
		menu.toggle(animated);
	}
	
	private void handleToggleRightView(boolean animated)
	{
		SlidingMenu menu = slidingMenu.get();
		menu.toggleSecondary(animated);
	}
	
	private void handleOpenLeftView(boolean animated)
	{
		SlidingMenu menu = slidingMenu.get();
		menu.showMenu(animated);
	}
	
	private void handleOpenRightView(boolean animated)
	{
		SlidingMenu menu = slidingMenu.get();
		menu.showSecondaryMenu(animated);
	}
	
	private void handleCloseLeftView(boolean animated)
	{
		SlidingMenu menu = slidingMenu.get();
		if (menu.isMenuShowing())
			menu.showContent(animated);
	}
	
	private void handleCloseRightView(boolean animated)
	{
		SlidingMenu menu = slidingMenu.get();
		if (menu.isSecondaryMenuShowing())
			menu.showContent(animated);
	}
	
	private void handleCloseViews(boolean animated)
	{
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

	@Override
	public boolean realUpdateOrientationModes()
	{
		if (hasProperty(AkylasSlidemenuModule.PROPERTY_CENTER_VIEW))
		{
			TiViewProxy proxy = ((TiViewProxy)getProperty(AkylasSlidemenuModule.PROPERTY_CENTER_VIEW));
			if (proxy instanceof TiWindowProxy)
			{
				if (((TiWindowProxy) proxy).realUpdateOrientationModes())
					return true;
			}
		}
		return super.realUpdateOrientationModes();
	}

	@Override
	public KrollProxy getParentForBubbling(TiWindowProxy proxy) {
		return this;
	}
}
