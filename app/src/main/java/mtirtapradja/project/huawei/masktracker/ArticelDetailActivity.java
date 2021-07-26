package mtirtapradja.project.huawei.masktracker;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import mtirtapradja.project.huawei.masktracker.Model.Article;


public class ArticelDetailActivity extends AppCompatActivity{

    private TextView title, author, contain;
    private ImageView image;
    private Article currentArticel;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);
        currentArticel = getIntent().getParcelableExtra("ARTICEL");

        toolbar = findViewById(R.id.articeldetail_toolbar);
        image = findViewById(R.id.articeldetail_image);
        title = findViewById(R.id.articeldetail_title);
        author = findViewById(R.id.articeldetail_author);
        contain = findViewById(R.id.articeldetail_contain);

        image.setImageResource(currentArticel.getImage());
        title.setText(currentArticel.getTitle());
        author.setText(currentArticel.getAuthor());
        contain.setText(currentArticel.getContain());
//        setSupportActionBar(toolbar);

    }

}