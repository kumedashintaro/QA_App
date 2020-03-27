package com.example.qa_app

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.util.Base64
import android.util.Log
import android.widget.ListView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FavoriteActivity : AppCompatActivity() {
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mListView: ListView
    private lateinit var mQuestionArrayList: ArrayList<Question>
    private lateinit var mAdapter: QuestionsListAdapter


    private var mFavoriteRef: DatabaseReference? = null
    private var mGenreIDRef: DatabaseReference? = null
    private var mQuestionIDRef: DatabaseReference? = null

    private var mGenre = 0
    private var value = 0


    private val mEventListener = object : ChildEventListener {
        val user = FirebaseAuth.getInstance().currentUser

        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            //ジャンルのID取得します。
            mGenreIDRef = mDatabaseReference.child(FavoritePath).child(user!!.uid).child(value.toString())

            //質問のID取得します。
            mQuestionIDRef= mDatabaseReference.child(FavoritePath).child(user!!.uid)


            val map = dataSnapshot.value as Map<String, String>
            val GenreID = map["GenreID"]?:""
            val QuestionID = map["QuestionID"]?:""

            //Firebaseにデータの確認をする処理を書く

            val userRef = mDatabaseReference.child(ContentsPATH).child(GenreID.toString()).child(QuestionID.toString())

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    //質問の情報を取得する。
                    val map = dataSnapshot.value as Map<String, String>
                    val title = map["title"] ?: ""
                    val body = map["body"] ?: ""
                    val name = map["name"] ?: ""
                    val uid = map["uid"] ?: ""
                    val imageString = map["image"] ?: ""
                    val bytes =
                        if (imageString.isNotEmpty()) {
                            Base64.decode(imageString, Base64.DEFAULT)
                        } else {
                            byteArrayOf()
                        }

                    //リストに追加する
                    val answerArrayList = ArrayList<Answer>()
                    val answerMap = map["answers"] as Map<String, String>?
                    if (answerMap != null) {
                        for (key in answerMap.keys) {
                            val temp = answerMap[key] as Map<String, String>
                            val answerBody = temp["body"] ?: ""
                            val answerName = temp["name"] ?: ""
                            val answerUid = temp["uid"] ?: ""
                            val answer = Answer(answerBody, answerName, answerUid, key)
                            answerArrayList.add(answer)
                        }
                    }
                    val question = Question(
                        title, body, name, uid, dataSnapshot.key ?: "",
                        mGenre, bytes, answerArrayList)
                    mQuestionArrayList.add(question)
                    mAdapter.notifyDataSetChanged()

                }
                override fun onCancelled(firebaseError: DatabaseError) {}
            })
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
        }
        override  fun onChildRemoved(p0: DataSnapshot) {
        }
        override fun onChildMoved(p0: DataSnapshot, p1: String?){
        }
        override fun onCancelled(p0: DatabaseError){
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite)

        // ListViewの準備
        mListView = findViewById(R.id.listView)
        mAdapter = QuestionsListAdapter(this)
        mQuestionArrayList = ArrayList<Question>()
        mAdapter.notifyDataSetChanged()

        mListView.setOnItemClickListener { parent, view, position, id ->
            // Questionのインスタンスを渡して質問詳細画面を起動する
            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("question", mQuestionArrayList[position])
            startActivity(intent)
        }

        //質問リストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
        mQuestionArrayList.clear()
        mAdapter.setQuestionArrayList(mQuestionArrayList)

        mListView.adapter = mAdapter

        val user = FirebaseAuth.getInstance().currentUser
        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().reference
        mFavoriteRef = mDatabaseReference.child(FavoritePath).child(user!!.uid)
        mFavoriteRef!!.addChildEventListener(mEventListener)
    }
}
