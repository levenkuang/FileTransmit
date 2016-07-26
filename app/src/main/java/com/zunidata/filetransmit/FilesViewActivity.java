package com.zunidata.filetransmit;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class FilesViewActivity extends Activity {
	private SimpleAdapter adapter;
	ArrayList<HashMap<String, Object>> listItem;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_view);
		
		Toast.makeText(getApplicationContext(), "chooose a file to transmit", Toast.LENGTH_SHORT).show();
		ListView fileView = (ListView)findViewById(R.id.fileView);
		listItem = new ArrayList<HashMap<String, Object>>();
		//ImageView image = (ImageView)findViewById(R.id.image);
		//image.setImageResource();
		this.FilesListView(Environment.getExternalStorageDirectory().getPath());
		adapter = new SimpleAdapter(
				getApplicationContext(),
				listItem,
				R.layout.item_view,
				new String[] {"image", "name", "path", "type", "parent"},
				new int[]{R.id.image, R.id.file_name, R.id.file_path, R.id.file_type, R.id.file_parent}
		);
		fileView.setAdapter(adapter);
		fileView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				TextView isDirectory = (TextView)view.findViewById(R.id.file_type); 
				TextView path = (TextView)view.findViewById(R.id.file_path);
				TextView name = (TextView)view.findViewById(R.id.file_name);
				
				if (Boolean.parseBoolean(isDirectory.getText().toString())){
					FilesListView(path.getText().toString());
					adapter.notifyDataSetChanged();
				}else{
					
					Intent intent = new Intent();
					intent.putExtra("FileName", name.getText().toString());
					intent.putExtra("FilePath", path.getText().toString());
					setResult(RESULT_OK, intent);
					finish();
				}
			}
		});
	}
	private void FilesListView(String selectedPath){
		File selectedFile = new File(selectedPath);
		if (selectedFile.canRead()){
			File[] file = selectedFile.listFiles();
			listItem.clear();
			for (int i = 0; i < file.length; i++){
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("image", file[i].isDirectory()?R.drawable.folder:R.drawable.file);
				map.put("name", file[i].getName());
				map.put("path", file[i].getPath());
				map.put("type", file[i].isDirectory());
				map.put("parent", file[i].getParent());
				
				listItem.add(map);
			}

			if (selectedFile.getParent() != null){
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("name", "Up");
				map.put("path", selectedFile.getParent());
				map.put("type", true);
				map.put("parent", selectedFile.getParent());
				listItem.add(0, map);
			}
		}else{
			Toast.makeText(getApplicationContext(), "foldet not readable", Toast.LENGTH_SHORT).show();
		}
	}

}
