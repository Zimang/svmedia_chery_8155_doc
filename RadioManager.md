`listModules`
```java
public int listModules(List<ModuleProperties> modules) {  
    if (modules == null) {  
        Log.e("BroadcastRadio.manager", "the output list must not be empty");  
        return -22;  
    } else {  
        Log.d("BroadcastRadio.manager", "Listing available tuners...");  
  
        List<ModuleProperties> returnedList;  
        try {  
            returnedList = this.mService.listModules();  
        } catch (RemoteException e) {  
            Log.e("BroadcastRadio.manager", "Failed listing available tuners", e);  
            return -32;  
        }  
  
        if (returnedList == null) {  
            Log.e("BroadcastRadio.manager", "Returned list was a null");  
            return Integer.MIN_VALUE;  
        } else {  
            modules.addAll(returnedList);  
            return 0;  
        }  
    }  
}
```