package com.example.ankit.chatappstable;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Ankit on 01-Mar-18.
 */

class SectionPagerAdapter extends FragmentPagerAdapter{

    public SectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0 :
                RequestFragment requestFragment = new RequestFragment();
                return requestFragment;
            case 1:
                ChatFragment chatFragment = new ChatFragment();
                return chatFragment;
            case 2 :
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;
            default:
                return null;
        }
    }


    public CharSequence getPageTitle(int position){

        switch (position){
            case 0 :
                return "Request";
            case 1:
                return "Chats";
            case 2 :
                return "Friends";
            default:
                return null;
        }

    }
    @Override
    public int getCount() {
        return 3;
    }
}
