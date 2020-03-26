package com.example.qa_app

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_question_detail.*
import kotlinx.android.synthetic.main.activity_question_detail.listView
import kotlinx.android.synthetic.main.content_main.*

import java.util.HashMap

class QuestionDetailActivity : AppCompatActivity() {

    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference
    private lateinit var mDataBaseReference: DatabaseReference

    var flag = true


    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>

            val answerUid = dataSnapshot.key ?: ""

            for (answer in mQuestion.answers) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid == answer.answerUid) {
                    return
                }
            }
            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
        override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
        override fun onCancelled(databaseError: DatabaseError) {}
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)

        // 渡ってきたQuestionのオブジェクトを保持する
        val extras = intent.extras
        mQuestion = extras.get("question") as Question

        title = mQuestion.title

        mDataBaseReference = FirebaseDatabase.getInstance().reference


        // ListViewの準備
        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        fab.setOnClickListener {
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // Questionを渡して回答作成画面を起動する
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", mQuestion)
                startActivity(intent)
            }
        }

        val dataBaseReference = FirebaseDatabase.getInstance().reference
        mAnswerRef = dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)
    }


    @SuppressLint("RestrictedApi")
    override fun onResume(){
        super.onResume()

        //ログインしていれば、お気に入り表示
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) {

            fab2.visibility = View.INVISIBLE

        } else {
            fab2.visibility = View.VISIBLE

            //質問IDを取得する

            val mdataBaseReference = FirebaseDatabase.getInstance().reference
            val favoriteRef = mdataBaseReference.child(FavoritePath).child(user!!.uid).child(mQuestion.questionUid)

            favoriteRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val data = snapshot.value as Map<*, *>?
                    if (data != null) {
                        data!!["value"] as String
                        //質問ID取得の有無でお気に入りしているか表示
                        fab2.setImageResource(R.drawable.ic_star_on)
                        flag = false
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                }
            })

        }
        //お気に入りが押された時　
        fab2.setOnClickListener {
            if(flag){
                fab2.setImageResource(R.drawable.ic_star_on)
                val dataBaseReference = FirebaseDatabase.getInstance().reference
                val favoriteRef = dataBaseReference.child(FavoritePath).child(user!!.uid).child(mQuestion.questionUid)
                val data = HashMap<String, String>()

                data["value"] = mQuestion.genre.toString()
                favoriteRef.setValue(data)
                flag = false
            }else{
                //Firebaseから削除
                val dataBaseReference = FirebaseDatabase.getInstance().reference
                dataBaseReference.child(FavoritePath).child(user!!.uid).child(mQuestion.questionUid).removeValue()

                fab2.setImageResource(R.drawable.ic_star_off)
                flag = true
            }
        }
    }
}