package com.abc.product.app.android;

import android.os.Bundle;
import android.widget.TableRow;

import com.abc.R;

import ai.api.AIServiceException;

class ShowReviewPageActivity extends BaseActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_review);
        TableRow row= new TableRow(this);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        row.setLayoutParams(lp);
        row.addView();

    }

    @Override
    public void onPermissionsGranted(int requestCode) throws AIServiceException {

    }
}
