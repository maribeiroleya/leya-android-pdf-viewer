/**
 * Copyright 2016 Bartosz Schiller
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.barteksc.sample;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.util.SizeF;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnActionEndListener;
import com.github.barteksc.pdfviewer.listener.OnDrawListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.listener.OnPageScrollListener;
import com.github.barteksc.pdfviewer.listener.OnPageSwipeChangeListener;
import com.github.barteksc.pdfviewer.listener.OnRenderListener;
import com.github.barteksc.pdfviewer.listener.OnTapListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.github.barteksc.pdfviewer.util.Hotspot;
import com.github.barteksc.pdfviewer.util.Note;
import com.github.barteksc.pdfviewer.util.TextLine;
import com.github.barteksc.pdfviewer.util.TextNote;

import com.google.gson.JsonArray;
import io.legere.pdfiumandroid.PdfDocument;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

public class PDFViewActivity extends AppCompatActivity implements OnPageChangeListener, OnLoadCompleteListener, OnPageErrorListener, OnDrawListener, OnActionEndListener, OnRenderListener, OnPageSwipeChangeListener, OnPageScrollListener, OnTapListener {

    private static final String TAG = PDFViewActivity.class.getSimpleName();

    private final static int REQUEST_CODE = 42;
    public static final int PERMISSION_CODE = 42042;

    public static final String SAMPLE_FILE = "9.pdf";
    public static final String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";

    private PDFView pdfView;

    private Uri uri;

    private Integer pageNumber = 0;

    private String pdfFileName;

    private String hotspotsString;
    private String notesString;
    private String textNotesString;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.pickFile:
                pickFile();

                break;
        }

        return true;
    }

    private void initViews() {
        pdfView = findViewById(R.id.pdfView);

        afterViews();
    }

    void pickFile() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                READ_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{READ_EXTERNAL_STORAGE},
                    PERMISSION_CODE
            );

            return;
        }

        launchPicker();
    }

    void launchPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        try {
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            //alert user that file manager not working
            Toast.makeText(this, R.string.toast_pick_file_error, Toast.LENGTH_SHORT).show();
        }
    }

    void afterViews() {
        pdfView.setBackgroundColor(Color.LTGRAY);
        if (uri != null) {
            displayFromUri(uri);
        } else {
            displayFromAsset(SAMPLE_FILE);
        }
        setTitle(pdfFileName);
    }

    public static JsonArray stringToArray(String string) {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();
        return gson.fromJson(string, JsonArray.class);
    }

    private void displayFromAsset(String assetFileName) {
        boolean isLandscape = false;
        int orientation = this.getResources().getConfiguration().orientation;
        isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE;
        pdfFileName = assetFileName;

        List<Hotspot> hotspots = new ArrayList<>();
        hotspots.add(new Hotspot(22.58064516129032, 35.80246913580247, "play"));
        hotspots.add(new Hotspot(32.25491431451613, 35.95679012345679, "game"));
        hotspots.add(new Hotspot(12.093623991935484, 38.269193672839506, "default"));
        hotspots.add(new Hotspot(0.0, 95.37037037037037, "activity"));
        hotspots.add(new Hotspot(9.07258064516129, 83.64198001814476, "link"));
        hotspots.add(new Hotspot(66.33064516129032, 64.043214586046, "key"));
        hotspots.add(new Hotspot(7.661290322580645, 8.333333333333332, "audio"));
        hotspots.add(new Hotspot(7.661290322580645, 18.333333333333332, "check_mark"));
        hotspots.add(new Hotspot(17.661290322580645, 18.333333333333332, "document"));
        hotspots.add(new Hotspot(17.661290322580645, 28.333333333333332, "image"));
        hotspots.add(new Hotspot(27.661290322580645, 28.333333333333332, "link"));
        hotspots.add(new Hotspot(27.661290322580645, 38.333333333333332, "bookmark"));


        List<TextNote> textNotes = new ArrayList<>();
        List<TextLine> lines1 = new ArrayList<>();
        TextLine line1 = new TextLine(24, "#000000", 1.0f, "Practice");
        lines1.add(line1);
        TextNote textNote1 = new TextNote(76.58132030558352, 56.71656202823219, 10.227475093577894, 2.391138281472756, "#FE7F00", 1.0f, "#000000",1, 1.0f, lines1, false);
        textNotes.add(textNote1);
        List<TextLine> lines2 = new ArrayList<>();
        TextLine line2 = new TextLine(84, "#000000", 1.0f, "Practice\nghg");
        lines2.add(line2);
        TextNote textNote2 = new TextNote(55.3, 10.5, 36.4, 12.4, "#FE7F00", 1.0f, "#000000",10, 0.5f, lines2, true);
        textNotes.add(textNote2);


        List<Note> notes = new ArrayList<>();
        notes.add(new Note(10, 10, "blue"));
        notes.add(new Note(40, 50, "red"));
        notes.add(new Note(0, 10, "blue"));
        notes.add(new Note(30, 50, "red"));
        notes.add(new Note(100, 10, "blue"));
        notes.add(new Note(20, 50, "red"));
        notes.add(new Note(90, 10, "blue"));
        notes.add(new Note(60, 50, "red"));
        notes.add(new Note(80, 10, "blue"));
        notes.add(new Note(45, 50, "red"));
        notes.add(new Note(10, 10, "blue"));




        pdfView.fromAsset(SAMPLE_FILE)
                .defaultPage(pageNumber)
                .enableMovement(true)
                .withHotspots(hotspots)
                .withNotes(notes)
                .withTextNotes(textNotes)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .onDraw(this)
                .onActionEnd(this)
                .onRender(this)
                .onPageScroll(this)
                .onPageSwipeChange(this)
                .onActionEnd(this)
                .landscapeOrientation(isLandscape)
                .dualPageMode(false)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(0) // in dp
                .enableSwipe(true)
                .swipeHorizontal(true)
                .pageFling(true)
                .fitEachPage(false)
                .onPageError(this)
                .pageFitPolicy(FitPolicy.BOTH)
                .load();
    }

    private void displayFromUri(Uri uri) {
        pdfFileName = getFileName(uri);

        pdfView.fromUri(uri)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(0) // in dp
                .dualPageMode(false)
                .enableSwipe(true)
                .swipeHorizontal(true)
                .pageFling(true)
                .onPageError(this)
                .load();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        onResult(resultCode, data);
    }

    public void onResult(int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            uri = intent.getData();
            displayFromUri(uri);
        }
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        setTitle(String.format("%s %s / %s", pdfFileName, page + 1, pageCount));
    }

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);

                    if (columnIndex >= 0) {
                        result = cursor.getString(columnIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
        for (PdfDocument.Bookmark b : tree) {

            Log.e(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

            if (!b.getChildren().isEmpty()) {
                printBookmarksTree(b.getChildren(), sep + "-");
            }
        }
    }

    /**
     * Listener for response to user permission request
     *
     * @param requestCode  Check that permission request code matches
     * @param permissions  Permissions that requested
     * @param grantResults Whether permissions granted
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchPicker();
            }
        }
    }

    @Override
    public void onPageError(int page, Throwable t) {
        Log.e(TAG, "Cannot load page " + page);
    }

    @Override
    public void onLayerDrawn(Canvas canvas, float pageWidth, float pageHeight, int displayedPage) {

    }

    @Override
    public void loadComplete(int nbPages) {
        SizeF pageSize = this.pdfView.getPageSize(0);
        float width = pageSize.getWidth();
        float height = pageSize.getHeight();
    }

    @Override
    public void actionEnd() {

        Log.d("ACTION_END", "1");

        SizeF pageSize = this.pdfView.getPageSize(0);
        float width = pageSize.getWidth();
        float height = pageSize.getHeight();
    }

    @Override
    public void onInitiallyRendered(int nbPages) {
        Log.d("ON_RENDER", "ON_RENDER");
    }

    @Override
    public void onPageSwipeChange(int offset) {
        Log.d("onPageSwipeChange", "0");
        if(Math.abs(offset) > 300*getResources().getDisplayMetrics().density) {
            if(offset > 0) {
                Log.d("onPageSwipeChange", "prevPage");

            }
            else {
                Log.d("onPageSwipeChange", "nextPage");
            }
        }
    }

    @Override
    public void onPageScrolled(int page, float positionOffset) {

    }

    @Override
    public boolean onTap(MotionEvent e) {
        return false;
    }

    @Override
    public void onPageScrolledEnd(float zoom) {
        Log.d("onPageScrolledEnd", "onPageScrolledEnd");
    }

}
