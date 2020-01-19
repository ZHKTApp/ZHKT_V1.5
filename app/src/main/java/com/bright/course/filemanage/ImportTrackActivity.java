package com.bright.course.filemanage;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bright.course.BaseEventBusActivity;
import com.bright.course.R;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;


public class ImportTrackActivity extends BaseEventBusActivity {

    ListView list;


    ArrayList<String> str = new ArrayList<>();
    // Check if the first level of the directory structure is the one showing
    private Boolean firstLvl = true;
    private String chosenFile;
    private Item[] fileList;
    private ListAdapter adapter;

    private File path = new File(Environment.getExternalStorageDirectory() + "/DCIM");

    public static void launch(Context context) {
        context.startActivity(new Intent(context, ImportTrackActivity.class));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);
        loadFileList();

        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ((TextView) findViewById(R.id.tvTitle)).setText("文件管理");
        list = findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                chosenFile = fileList[position].file;
                File sel = new File(path + "/" + chosenFile);
                if (sel.isDirectory()) {
                    firstLvl = false;
                    // Adds chosen directory to list
                    str.add(chosenFile);
                    fileList = null;
                    path = new File(sel + "");

                    loadFileList();
                    list.setAdapter(adapter);

                }

                // Checks if 'up' was clicked
                else if (chosenFile.equalsIgnoreCase("向上") && !sel.exists()) {

                    // present directory removed from list
                    String s = str.remove(str.size() - 1);

                    // path modified to exclude present directory
                    path = new File(path.toString().substring(0, path.toString().lastIndexOf(s)));
                    fileList = null;

                    // if there are no more directories in the list, then
                    // its the first level
                    if (str.isEmpty()) {
                        firstLvl = true;
                    }
                    loadFileList();
                    list.setAdapter(adapter);

                } else if (chosenFile.endsWith(".gpx")) {
                }
            }
        });
    }


    private void loadFileList() {
        try {
            path.mkdirs();
        } catch (SecurityException e) {
            Log.e("", "unable to write on the sd card ");
        }

        // Checks whether path exists
        if (path.exists()) {
            FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    // Filters based on whether the file is hidden or not
                    return (sel.isFile() || sel.isDirectory()) && !sel.isHidden();

                }
            };

            String[] fList = path.list(filter);
            fileList = new Item[fList.length];
            for (int i = 0; i < fList.length; i++) {
                fileList[i] = new Item(fList[i], R.drawable.common_file_icon);

                // Convert into file path
                File sel = new File(path, fList[i]);

                // Set drawables
                if (sel.isDirectory()) {
                    fileList[i].icon = R.drawable.ic_type_folder;
                    // Ut.dd("DIRECTORY", fileList[i].file);
                } else if (fileList[i].file.contains(".gpx")) {
                    fileList[i].icon = R.drawable.common_file_icon;
                }
            }

            if (!firstLvl) {
                Item temp[] = new Item[fileList.length + 1];
                System.arraycopy(fileList, 0, temp, 1, fileList.length);
                temp[0] = new Item("向上", R.drawable.ic_back_folder);
                fileList = temp;
            }
        } else {
            Log.e("", "path does not exist");
        }

        adapter = new ArrayAdapter<Item>(this, android.R.layout.select_dialog_item, android.R.id.text1, fileList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // creates view
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);

                // put the image on the text view
                textView.setCompoundDrawablesWithIntrinsicBounds(fileList[position].icon, 0, 0, 0);
                // add margin between image and text (support various screen
                // densities)
                int dp5 = (int) (5 * getResources().getDisplayMetrics().density + 0.5f);
                textView.setCompoundDrawablePadding(dp5);

                return view;
            }
        };

    }

    private class Item {
        public String file;
        public int icon;

        public Item(String file, Integer icon) {
            this.file = file;
            this.icon = icon;
        }

        @Override
        public String toString() {
            return file;
        }
    }

}
