
package org.melato.android.menu;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.melato.android.menu.MenuCapture.Item;

import android.view.MenuItem;

class MenuItemInvocationHandler implements InvocationHandler {
  private Item item;  

  public MenuItemInvocationHandler(Item item) {
    super();
    this.item = item;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args)
      throws Throwable {    
    if ( MenuItem.class.equals(method.getReturnType())) {
      if ( "setIcon".equals(method.getName())) {
        item.icon = (Integer) args[0];
      }
      return proxy;
    }
    return null;
  }

}
