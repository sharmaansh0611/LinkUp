package com.example.instademo.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.instademo.R
import com.example.instademo.adapter.MyReelRvAdapter
import com.example.instademo.databinding.FragmentMyReelsBinding
import com.example.instademo.model.Reel
import com.example.instademo.utils.REEL
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase


class myReelsFragment : Fragment() {
    private lateinit var binding:FragmentMyReelsBinding
    private lateinit var reelList: ArrayList<Reel>
    private lateinit var adapter: MyReelRvAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMyReelsBinding.inflate(inflater, container, false)
        reelList = ArrayList()
        var adapter = MyReelRvAdapter(requireContext(),reelList)
        binding.rv.layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        binding.rv.adapter = adapter

        Firebase.firestore.collection(Firebase.auth.currentUser!!.uid+REEL).get().addOnSuccessListener {
            var tempList = ArrayList<Reel>()
            for (i in it.documents){
                var reel:Reel = i.toObject<Reel>()!!
                tempList.add(reel)

            }
            reelList.addAll(tempList)
            adapter.notifyDataSetChanged()
        }



        return binding.root

    }

    companion object {

    }
}