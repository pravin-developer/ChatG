package com.pravin.chatg;

import android.provider.ContactsContract;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.security.acl.Group;

public class TestAccessAdapter extends FragmentPagerAdapter {

    public TestAccessAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                ChatFragment chatFragment=new ChatFragment();
                return chatFragment;
            case 1:
                GroupFragment groupFragment=new GroupFragment();
                return groupFragment;
            case 2:
                ContactsFragment contactsFragment=new ContactsFragment();
                return contactsFragment;
            case 3:
                RequestsFragment requestsFragment=new RequestsFragment();
                return requestsFragment;
                default:
                    return null;
        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position)
        {
            case 0:
              return "Chats";
            case 1:
                return "Groups";
            case 2:
                return "Contacts";
            case 3:
                return "Requests";

            default:
                return null;
        }

    }
}
