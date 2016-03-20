package com.ladjzero.uzlee.utils;

import android.os.AsyncTask;

import com.ladjzero.hipda.ProgressReporter;

/**
 * Created by chenzhuo on 16-3-19.
 */
public abstract class ReportableAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> implements ProgressReporter {

}
