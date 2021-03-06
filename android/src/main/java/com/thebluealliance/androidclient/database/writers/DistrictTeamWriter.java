package com.thebluealliance.androidclient.database.writers;

import com.thebluealliance.androidclient.database.Database;
import com.thebluealliance.androidclient.models.DistrictRanking;

import android.support.annotation.WorkerThread;

import javax.inject.Inject;

public class DistrictTeamWriter extends BaseDbWriter<DistrictRanking> {
    @Inject
    public DistrictTeamWriter(Database db) {
        super(db);
    }

    @Override
    @WorkerThread
    public void write(DistrictRanking districtTeam, Long lastModified) {
        mDb.getDistrictTeamsTable().add(districtTeam, lastModified);
    }
}