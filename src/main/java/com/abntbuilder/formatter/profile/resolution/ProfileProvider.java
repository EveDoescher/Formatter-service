package com.abntbuilder.formatter.profile.resolution;

import com.abntbuilder.formatter.profile.model.DocumentProfile;

public interface ProfileProvider {

    DocumentProfile findById(String profileId);
}
