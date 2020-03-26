package com.example.qa_app

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.widget.ListView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class FavoriteActivity : AppCompatActivity() {
    private lateinit var  mFavoriteListAdapter: FavoriteListAdapter

    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mListView: ListView
    private lateinit var mFavoriteArrayList: ArrayList<Favorite>

    private var mFavoriteRef: DatabaseReference? = null
    private lateinit var mFavorite: Favorite
    val user = FirebaseAuth.getInstance().currentUser

    private var mGenre = 0

    private val mEventListener = object : ChildEventListener {

        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {

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
            val favorite = Favorite(
                title, body, name, uid, dataSnapshot.key ?: "",
                mGenre, bytes, answerArrayList)
            mFavoriteArrayList.add(favorite)
            mFavoriteListAdapter.notifyDataSetChanged()


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


        mFavoriteRef = mDatabaseReference.child(FavoritePath).child(user!!.uid).child(mFavorite.questionUid)
        mFavoriteRef!!.addChildEventListener(mEventListener)



        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().reference

        // ListViewの準備
        mListView = findViewById(R.id.listView)
        mFavoriteListAdapter = FavoriteListAdapter(this)
        mFavoriteArrayList = ArrayList<Favorite>()
        mFavoriteListAdapter.notifyDataSetChanged()

        //質問リストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
        mFavoriteArrayList.clear()
        mFavoriteListAdapter.setFavoriteArrayList(mFavoriteArrayList)

        mListView.adapter = mFavoriteListAdapter

    }
}
