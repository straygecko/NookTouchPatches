package com.android.server.status;

import java.lang.reflect.Method;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.ImageView;

public class ModUtils {
  
  private static final String VERSION          = "0.1.0";
  private static final String TAG              = "NookMod";

  
  /* 1.2.0 constants */
  private static final int QUICKNAV_HOME       = 0x102019E;
  private static final int QUICKNAV_LIBRARY    = 0x102019F;
  private static final int QUICKNAV_SHOP       = 0x10201A0;
  private static final int QUICKNAV_SEARCH     = 0x10201A1;
  private static final int QUICKNAV_SETTINGS   = 0x10201A2;
  private static final int QUICKNAV_GLOWLIGHT  = 0x10201A3;
  private static final int STATUSBAR_BACK      = 0x1020221;
  private static final int STATUSBAR_MENU      = 0x1020222;
  private static final int STATUSBAR_READNOW   = 0x1020223;
  private static final int STATUSBAR_NOTIFY    = 0x1020224;
  private static final int STATUSBAR_GLOWLIGHT = 0x1020226;

  /* 1.1.5 constants */
  /*
  private static final int QUICKNAV_HOME       = 0x1020196;
  private static final int QUICKNAV_LIBRARY    = 0x1020197;
  private static final int QUICKNAV_SHOP       = 0x1020198;
  private static final int QUICKNAV_SEARCH     = 0x1020199;
  private static final int QUICKNAV_SETTINGS   = 0x102019A;
  private static final int QUICKNAV_GLOWLIGHT  = 0x102019B;
  private static final int STATUSBAR_BACK      = 0x1020220;
  private static final int STATUSBAR_MENU      = 0x1020221;
  private static final int STATUSBAR_READNOW   = 0x1020222;
  private static final int STATUSBAR_NOTIFY    = 0x1020223;
  private static final int STATUSBAR_GLOWLIGHT = 0x1020225;
  */

  public static String getVersion() { return VERSION; }

  private static String getButtonName(int id) {
    switch (id)
    {
      case QUICKNAV_HOME:       return "quicknav.1";
      case QUICKNAV_LIBRARY:    return "quicknav.2";
      case QUICKNAV_SHOP:       return "quicknav.3";
      case QUICKNAV_SEARCH:     return "quicknav.4";
      case QUICKNAV_SETTINGS:   return "quicknav.5";
      case QUICKNAV_GLOWLIGHT:  return "quicknav.6";
      case STATUSBAR_BACK:      return "statusbar.back";
      case STATUSBAR_MENU:      return "statusbar.menu";
      case STATUSBAR_READNOW:   return "statusbar.readnow";
      case STATUSBAR_NOTIFY:    return "statusbar.notify";
      case STATUSBAR_GLOWLIGHT: return "statusbar.glowlight";
    }
    return null;
  }

  public static boolean OnButtonClick(Context context, int id) {
    String buttonName = getButtonName(id);
    if(buttonName == null || buttonName.length() == 0)
    {
      Log.e(TAG, "OnButtonClick() Unknown button id = " + id);
      return false;
    }
    //Log.v(TAG, "OnButtonClick() button = " + buttonName);
    
    ContentResolver resolver = context.getContentResolver();
    String action = Settings.System.getString(resolver, "mod." + buttonName + ".cmd");
    
    if ( action == null || action.length() == 0)
      return false;
    
    return doAction(context, action);
  }

  public static boolean alwaysShowStatusIcons(Context context) {
    ContentResolver resolver = context.getContentResolver();
    try {
      return ( Settings.System.getInt(resolver, "mod.option.always_show_status_icons") > 0 );
    } catch (SettingNotFoundException e) {
      return false;
    }
  }

  public static boolean disableDragToUnlock(Context context) {
    ContentResolver resolver = context.getContentResolver();
    try {
      return ( Settings.System.getInt(resolver, "mod.option.disable_drag_to_unlock") > 0 );
    } catch (SettingNotFoundException e) {
      return false;
    }
  }

  public static Boolean overrideShopButton(Context context)
  {
    return ( getButtonBitmap(context, "QUICKNAV_SHOP") != null);
  }
  
  public static void replaceQuicknavIcon(Context context, ImageView view) throws NameNotFoundException {

    if(view == null || view.getId() == 0)
      return;

    String buttonName = getButtonName( view.getId() );
    if(buttonName == null || buttonName.length() == 0)
      return;

    //Log.v(TAG, "replaceQuicknavIcon() button = " + buttonName);
    
    Bitmap icon      = getButtonBitmap(context, buttonName);
    String iconLabel = getButtonLabel(context, buttonName);

    if ( (iconLabel == null || iconLabel.length() == 0) && (icon == null) )
      return;
    
    WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    Display display = wm.getDefaultDisplay();

    ContentResolver resolver = context.getContentResolver();
    boolean bHasGlowlight = ( Integer.parseInt(Settings.System.getString(resolver, "has_glowlight")) > 0 );
    int buttonCount = bHasGlowlight ? 6 : 5;
    int buttonWidth = display.getWidth() / buttonCount;
    int buttonHeight = 112;

    int labelOffsetY = 86;
    int iconOffsetY  = 96;
    
      Canvas canvas = new Canvas();
      Paint paint = new Paint();
      paint.setColor(Color.BLACK);

      Bitmap newBitmap = Bitmap.createBitmap(buttonWidth, buttonHeight, Bitmap.Config.RGB_565);
      canvas.setBitmap(newBitmap);
      
      canvas.drawColor(Color.WHITE);

      paint.setStrokeWidth(12);
      canvas.drawLine(0, 0, buttonWidth-1, 0, paint);
      
      paint.setStrokeWidth(1);
      if ( view.getId() != QUICKNAV_SETTINGS)
        canvas.drawLine(buttonWidth-1, 0, buttonWidth-1, buttonHeight, paint);
            
      if (icon != null) {
        int offsetX = (newBitmap.getWidth() - icon.getWidth()) / 2;
        int offsetY = (iconOffsetY - icon.getHeight()) / 2;
        if (offsetY < 0)
          offsetY = 0;
        canvas.drawBitmap(icon, offsetX, offsetY, null);
      }

    if(iconLabel != null && iconLabel.length() > 0)
    {
        paint.setTypeface(Typeface.DEFAULT);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(20);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);

        float tWidth = paint.measureText(iconLabel);
        if ( tWidth > buttonWidth ) {
          String ellipsis = "�";
          float eWidth = paint.measureText(ellipsis);

          int len = iconLabel.length();
            while ( tWidth+eWidth >= buttonWidth && len-- > 0 ) {
                iconLabel = iconLabel.substring(0, len);
                tWidth = paint.measureText(iconLabel);
            }
            iconLabel += ellipsis;
        }

        canvas.drawText(iconLabel, buttonWidth/2, labelOffsetY, paint);
    }
      
        view.setImageBitmap(newBitmap);
  }

  private static Bitmap getButtonBitmap(Context context, String buttonName)
  {
    if(buttonName == null || buttonName.length() == 0)
      return null;

    ContentResolver resolver = context.getContentResolver();
    String bitmapId = Settings.System.getString(resolver, "mod." + buttonName + ".icon" );
    
    //Log.v(TAG, "icon = " + bitmapId);

    if (bitmapId == null || bitmapId.length() == 0)
        return null;
    
    if (bitmapId.matches("^\\d+$"))
    {
      Resources res = context.getResources();
      return BitmapFactory.decodeResource(res, Integer.parseInt(bitmapId));
    }
    
    PackageManager pm = context.getPackageManager();
    Drawable icon;
    try {
      icon = pm.getApplicationIcon( bitmapId );
    } catch (NameNotFoundException e) {
      return null;
    }

    return ((BitmapDrawable) icon).getBitmap();
  }

  private static String getButtonLabel(Context context, String buttonName)
  {
    if(buttonName == null || buttonName.length() == 0)
      return null;

    ContentResolver resolver = context.getContentResolver();
    return Settings.System.getString(resolver, "mod." + buttonName + ".label" );
  }

  
  
  
  public static boolean doAction(final Context context, final String action) {
    //Log.v(TAG, "doAction() " + " action = " + action);
    if(action == null || action.length() == 0)
      return false;
    
    if (action.startsWith("PACKAGE:"))
      return doPackage(context, action.substring("PACKAGE:".length()));

    else if (action.startsWith("ACTIVITY:"))
      return doActivity(context, action.substring("ACTIVITY:".length()));

    else if (action.startsWith("BROADCAST:"))
      return doBroadcast(context, action.substring("BROADCAST:".length()));

    else if (action.startsWith("KEY:") || action.startsWith("LONGKEY:")) {
      new Thread(new Runnable() {
        public void run() {
          doKey(context, Integer.parseInt( action.substring("KEY:".length()) ), action.startsWith("LONGKEY:") );
        }
      }).start();       
      return true;
    }
    
    else if (action.equals("QUICKNAV"))
      return doToggleQuicknav(context);

    else if (action.equals("GLOWLIGHT"))
      return doToggleGlowlight(context);

    Log.v(TAG, "doAction() unknown action: " + action);
    return false;
  }
  
  private static Intent getIntentFromString(String intentStr) {        
    String chunk[] = intentStr.split(";");
    Intent intent = new Intent(chunk[0]);
    Log.v(TAG, "new intent: " + chunk[0]);

    for(int i=1; i < chunk.length ; i++)
    {
      if (chunk[i].startsWith("CATEGORY:"))
      {
        Log.v(TAG, "intent - adding category: " + chunk[i].substring(9));
        intent.addCategory( chunk[i].substring(9) );
      }

      if (chunk[i].startsWith("FLAGS:"))
      {
        Log.v(TAG, "intent - adding flags: " + chunk[i].substring(6));
        intent.setFlags( Integer.parseInt(chunk[i].substring(6)) );
      }
    }
    return intent;
  }
  
  private static boolean doPackage(Context context, String command) {
    if(command == null || command.length() == 0)
      return false;

    //Log.v(TAG, "doPackage() " + command );

    Intent intent = context.getPackageManager().getLaunchIntentForPackage(command);
    if (intent == null) {
      Log.v(TAG, "doPackage() cannot find intent for " + command );
      return false;
    }
        context.startActivity(intent);
    return true;
  }

  private static boolean doActivity(Context context, String command) {
    if(command == null || command.length() == 0)
      return false;

        context.startActivity( getIntentFromString( command ) );
    return true;
  }

  private static boolean doBroadcast(Context context, String command) {
    if(command == null || command.length() == 0)
      return false;

        context.sendBroadcast( getIntentFromString( command ) );
      return true;
  }

  private static boolean doKey(Context context, int keycode, Boolean longpress) {
    if(keycode <= 0)
      return false;

    Object windowman;
    IBinder wmbinder;
    Method injectKeyEvent;

    try {
      // wmbinder = ServiceManager.getService("window");
        wmbinder = (IBinder)Class.forName("android.os.ServiceManager").getMethod("getService", String.class).invoke(null, "window");
        // windowman = IWindowManager.Stub.asInterface( wmbinder );
        windowman = Class.forName("android.view.IWindowManager$Stub").getMethod("asInterface", IBinder.class).invoke(null, wmbinder);
        injectKeyEvent = Class.forName("android.view.IWindowManager").getMethod("injectKeyEvent", KeyEvent.class, boolean.class);

        injectKeyEvent.invoke(windowman, new KeyEvent( KeyEvent.ACTION_DOWN, keycode ), false);
        injectKeyEvent.invoke(windowman, new KeyEvent( KeyEvent.ACTION_UP,   keycode ), false);
    } catch (Exception e) {
        Log.v(TAG, "doKey failed");
        return false;
    }
    
    //Log.v(TAG, "doKey complete");
    return true;
  }

  private static boolean doToggleQuicknav(Context context) {
    Object statusbar = context.getSystemService("statusbar");
    try {
      // StatusBarManager.toggleQuickNav
      Class.forName("android.app.StatusBarManager").getMethod("toggleQuickNav").invoke(statusbar);
    } catch (Exception e) {
      //Log.v(TAG, "toggle quicknav failed " + e.toString() + "\n" + e.getCause() );
      return false;
    }
      return true;
  }

  private static boolean doToggleGlowlight(Context context)
  {
    com.android.internal.policy.impl.PhoneWindowManager wms = com.android.internal.policy.impl.ModUtils.getContext();
    try {
      Method toggleLights = Class.forName("com.android.internal.policy.impl.PhoneWindowManager").getDeclaredMethod("toggleLights");
      toggleLights.setAccessible(true);
      toggleLights.invoke(wms);
    } catch (Exception e) {
      Log.v(TAG, "toggle light failed " + e.toString() + "\n" + e.getCause() );
      return false;
    }
      return true;
  }
  
}
