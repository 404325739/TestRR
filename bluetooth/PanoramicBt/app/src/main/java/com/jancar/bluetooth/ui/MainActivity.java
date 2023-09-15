package com.jancar.bluetooth.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.jancar.bluetooth.R;
import com.jancar.bluetooth.utils.PBDownLoadStateUtil;
import com.jancar.sdk.utils.Logcat;

/**
 * @author
 * @date 2019-12-24 19:44:11
 */
public class MainActivity extends BaseMainActivity {
    @Override
    public void onBackPressed() {
//        String downstate = SystemPropertiesUtil.get("persist.sys.btpb.downstate","0");
        if(PBDownLoadStateUtil.isDownLoading()){
            showTipDialog();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    private void showTipDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(android.R.string.dialog_alert_title);
        builder.setMessage(R.string.tips_downloading_alert);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
//        dialog.setButton("取消", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//        dialog.setButton("确认", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                finish();
//            }
//        });
        AlertDialog dialog = builder.create();

        //设置层级
        Window window = dialog.getWindow();
        if (window != null) {
            Logcat.d("dialog window is not null");
            window.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        }
        dialog.show();
        Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        button.setTextColor(getResources().getColor(R.color.blue_checked_color));
        Button nbutton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        nbutton.setTextColor(getResources().getColor(R.color.blue_checked_color));

    }
}
