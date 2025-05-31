package com.inhatc.plogging;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class InfoFragment extends Fragment {

    int[] images = {
            R.drawable.stretch1,
            R.drawable.stretch2,
            R.drawable.stretch3,
            R.drawable.stretch4,
    };
    String[] titles = {
            "팔 뻗기",
            "목 스트레칭",
            "허리 늘리기",
            "팔 위로 들어올리기"
    };
    String[] descriptions = {
            "한쪽 팔을 반대편 가슴 쪽으로 가져오고, 다른 팔로 팔꿈치를 당겨주세요.\n그 상태에서 10초간 유지하며 어깨와 상체 근육을 이완합니다.",
            "고개를 천천히 좌우로 돌려 목 근육을 이완해줍니다.\n귀가 어깨에 닿도록 천천히 기울이세요.\n앞뒤로도 살짝씩 움직여 전반적인 긴장을 풀어줍니다.",
            "양 손을 허리에 얹고, 상체를 천천히 좌우 회전합니다.\n호흡을 내쉬며 부드럽게 돌리고, 무리하지 않도록 주의하세요.",
            "양팔을 머리 위로 올려 기지개를 켜듯 스트레칭합니다.\n기본적인 전신 이완 동작입니다."
    };

    ImageView imgLarge;
    TextView tvTitle, tvInfo;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imgLarge = view.findViewById(R.id.img_stretch);
        tvTitle = view.findViewById(R.id.tv_title);
        tvInfo = view.findViewById(R.id.tv_info);

        // 초기 세팅: 첫 번째 이미지와 텍스트
        setContent(0);

        // 썸네일 이미지뷰 3개 예시 (xml에 작게 배치한다고 가정)
        ImageView thumb1 = view.findViewById(R.id.thumb1);
        ImageView thumb2 = view.findViewById(R.id.thumb2);
        ImageView thumb3 = view.findViewById(R.id.thumb3);
        ImageView thumb4 = view.findViewById(R.id.thumb4);

        thumb1.setImageResource(images[0]);
        thumb2.setImageResource(images[1]);
        thumb3.setImageResource(images[2]);
        thumb4.setImageResource(images[3]);

        thumb1.setOnClickListener(v -> setContent(0));
        thumb2.setOnClickListener(v -> setContent(1));
        thumb3.setOnClickListener(v -> setContent(2));
        thumb4.setOnClickListener(v -> setContent(3));
    }

    private void setContent(int index) {
        imgLarge.setImageResource(images[index]);
        tvTitle.setText(titles[index]);
        tvInfo.setText(descriptions[index]);
    }
}
