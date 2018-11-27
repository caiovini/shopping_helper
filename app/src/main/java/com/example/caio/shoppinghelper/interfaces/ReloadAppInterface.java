package com.example.caio.shoppinghelper.interfaces;

 /*
    This interface is used to reload the app after the user pays
    The payment fragment is being detached when switching between different
    fragments, using this interface to go back to MainActivity will avoid this problem
 */

public interface ReloadAppInterface {

    void reloadApp();
}
