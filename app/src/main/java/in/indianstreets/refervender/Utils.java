package in.indianstreets.refervender;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import java.io.File;


/**
 * Created by Dell on 04-01-2017.
 */

public class Utils {

    public static boolean isDirExists(){
        File xmlStorage = new File(Environment.getExternalStorageDirectory(),Const.DIR_NAME);
        if(xmlStorage.exists() && xmlStorage.isDirectory()){
            return true;
        }
        return false;
    }

    public static File createDirectory(Context context){
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(),Const.DIR_NAME);
//        Log.d(Const.TAG,"dir path: "+mkmydir.getAbsolutePath());
        if (!mediaStorageDir.exists()){
            mediaStorageDir.mkdirs();
            Log.d(Const.TAG,"new dir created for xls");
//            Log.d(Const.TAG,"1) dir length: "+mediaStorageDir.listFiles().length);
        }else{
            Log.d(Const.TAG,"alredy folder exists");
//            Log.d(Const.TAG,"2) dir length: "+mediaStorageDir.listFiles().length);
        }

        return mediaStorageDir;
    }
}
