package com.example.safegallery.tabs.fragments;

import android.content.Context;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.safegallery.R;
import com.example.safegallery.recycler_views.folder.FolderAdapter;
import com.example.safegallery.recycler_views.interfaces.BottomSheetListener;
import com.example.safegallery.tabs.data.DataPath;
import com.example.safegallery.tabs.data.DataType;
import com.example.safegallery.tabs.viewmodels.DataViewModel;
import com.example.safegallery.tabs.viewmodels.DataViewModelFactory;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FolderFragment extends Fragment {

    int position;
    boolean safe;
    DataType dataType;
    DataViewModel viewModel;

    Context context;
    RecyclerView recyclerView;
    FolderAdapter recyclerViewAdapter;
    BottomSheetBehavior<View> bsSelectTools;
    ProgressBar progressBar;

    public FolderFragment() {
        // required
    }

    public FolderFragment(int position, boolean safe, DataType dataType) {
        this.position = position;
        this.safe = safe;
        this.dataType = dataType;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.recyclerViewAdapter = new FolderAdapter();
        this.recyclerViewAdapter.setContext(this.getContext());
        this.recyclerViewAdapter.setDataType(this.dataType);
        this.recyclerViewAdapter.setSafe(this.safe);
        this.recyclerViewAdapter.setDataMap(new HashMap<>());
        this.recyclerViewAdapter.setViewModelListener((ParentFragment)this.getParentFragment());

        this.viewModel = new ViewModelProvider(this.requireActivity(), new DataViewModelFactory(this.requireActivity().getApplication())).get(DataViewModel.class);

        this.setListeners();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_default, container, false);
        LinearLayout bsSelectToolsView = view.findViewById(R.id.bottom_sheet);

        this.setChildrenClickListeners(bsSelectToolsView);
        if (!safe)
            view.findViewById(R.id.unlock).setVisibility(View.GONE);
        else
            view.findViewById(R.id.lock).setVisibility(View.GONE);

        this.progressBar = view.findViewById(R.id.pbLoading);
        Map<String, List<DataPath>> data = this.viewModel.getDataMap(this.position).getValue();
        if (data == null)
            this.progressBar.setVisibility(View.VISIBLE);

        this.recyclerView = view.findViewById(R.id.rvDataRecyclerView);
        this.recyclerView.setLayoutManager(new GridLayoutManager(this.getContext(), 3));
        this.recyclerView.setAdapter(this.recyclerViewAdapter);

        this.bsSelectTools = BottomSheetBehavior.from(bsSelectToolsView);
        this.bsSelectTools.setState(BottomSheetBehavior.STATE_HIDDEN);

        this.recyclerViewAdapter.setBsSelectTools(this.bsSelectTools);

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        this.cancelSelecting();
    }

    private void setListeners() {
        this.recyclerViewAdapter.setClickListener(dataPath -> {
            Map<String, List<DataPath>> vmData = this.viewModel.getDataMap(this.position).getValue();
            List<DataPath> value = vmData == null
                    ? new ArrayList<>()
                    : vmData.get(dataPath.getPath());

            Map<String, List<DataPath>> data = new HashMap<>();
            data.put(dataPath.getPath(), value);

            Fragment fragment = new FileFragment(data, this.safe, this.dataType);
            this.getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.childHolder, fragment)
                    .addToBackStack(fragment.toString())
                    .commit();
        });

        this.viewModel.getDataMap(this.position).observe(requireActivity(), data -> {
            this.recyclerViewAdapter.setDataMap(data);
            this.recyclerViewAdapter.notifyDataSetChanged();

            if (this.recyclerView != null)
                this.recyclerView.setItemViewCacheSize(data.size());

            if (this.progressBar != null)
                this.progressBar.setVisibility(View.GONE);
        });
    }

    private void setChildrenClickListeners(LinearLayout view) {
        for (int i = 0; i < view.getChildCount(); i++) {
            view.getChildAt(i).setOnClickListener(v -> {
                BottomSheetListener listener = this.recyclerViewAdapter;
                listener.onChildClick(v.getId(), getParentFragmentManager());
            });
        }
    }

    private void cancelSelecting() {
        this.recyclerViewAdapter.setSelecting(false);
        this.recyclerViewAdapter.notifyDataSetChanged();
        this.bsSelectTools.setState(BottomSheetBehavior.STATE_HIDDEN);
    }
}
