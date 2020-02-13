package jp.fairydevices.mimi.example.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import jp.fairydevices.mimi.example.R;
import jp.fairydevices.mimi.example.model.TranslationHistory;

public class TranslationBookmarkAdapter extends RecyclerView.Adapter<TranslationBookmarkAdapter.ViewHolder> {

    private ArrayList<TranslationHistory> mData = null ;

    // 아이템 뷰를 저장하는 뷰홀더 클래스.
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textview_before_translate;
        TextView textview_after_translate;

        ViewHolder(View itemView) {
            super(itemView) ;

            // 뷰 객체에 대한 참조. (hold strong reference)
            textview_before_translate = itemView.findViewById(R.id.textview_before_translate) ;
            textview_after_translate = itemView.findViewById(R.id.textview_after_translate) ;
        }
    }

    // 생성자에서 데이터 리스트 객체를 전달받음.
    public TranslationBookmarkAdapter(ArrayList<TranslationHistory> list) {
        mData = list ;
    }

    // onCreateViewHolder() - 아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴.
    @Override
    public TranslationBookmarkAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext() ;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ;

        View view = inflater.inflate(R.layout.layout_translation_history, parent, false) ;
        TranslationBookmarkAdapter.ViewHolder vh = new TranslationBookmarkAdapter.ViewHolder(view) ;

        return vh ;
    }

    // onBindViewHolder() - position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시.
    @Override
    public void onBindViewHolder(TranslationBookmarkAdapter.ViewHolder holder, int position) {
        String text = mData.get(position).getBeforeString();
        String text2 = mData.get(position).getAfterString();

        holder.textview_before_translate.setText(text) ;
        holder.textview_after_translate.setText(text2) ;
    }

    // getItemCount() - 전체 데이터 갯수 리턴.
    @Override
    public int getItemCount() {
        return mData.size() ;
    }
}
