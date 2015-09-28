package de.fau.cs.mad.fablab.android.model;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.QueryBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.fau.cs.mad.fablab.android.model.events.ProjectSavedEvent;
import de.fau.cs.mad.fablab.android.viewmodel.common.Project;
import de.fau.cs.mad.fablab.rest.myapi.ProjectsApi;
import de.greenrobot.event.EventBus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ProjectModel {

    private ProjectsApi mProjectsApi;
    private RuntimeExceptionDao<Project, Long> mProjectDao;
    private EventBus mEventBus = EventBus.getDefault();

    private Callback<String> mSaveProjectCallback = new Callback<String>() {
        @Override
        public void success(String id, Response response) {
            System.out.println("create Project gist id: " + id);
            mEventBus.post(new ProjectSavedEvent(id, true));
        }

        @Override
        public void failure(RetrofitError error) {
            mEventBus.post(new ProjectSavedEvent(null, false));
        }
    };

    private Callback<String> mUpdateProjectCallback = new Callback<String>() {
        @Override
        public void success(String id, Response response) {
            System.out.println("update Project gist id: " + id);
            mEventBus.post(new ProjectSavedEvent(id, true));
        }

        @Override
        public void failure(RetrofitError error) {
            mEventBus.post(new ProjectSavedEvent(null, false));
        }
    };

    public ProjectModel(ProjectsApi projectsApi, RuntimeExceptionDao<Project, Long> projectDao) {
        mProjectsApi = projectsApi;
        mProjectDao = projectDao;
    }

    public void saveProject(Project project)
    {
        mProjectDao.createOrUpdate(project);
        mEventBus.post(new ProjectSavedEvent(null, true));
    }

    public void uploadProjectGithub(Project project)
    {
        if(project.getGistID() == null)
        {
            System.out.println("create Project");
            mProjectsApi.createProject(project.getProjectFile(), mSaveProjectCallback);
        }
        else
        {
            System.out.println("update Project");
            mProjectsApi.updateProject(project.getGistID(), project.getProjectFile(), mUpdateProjectCallback);
        }
    }

    public List<Project> getAllProjects()
    {
        List<Project> fetchedProjects = new ArrayList<>();
        //get next Element_count elements from database
        QueryBuilder<Project, Long> queryBuilder = mProjectDao.queryBuilder();
        //sort elements in descending order according to last_updated
        queryBuilder.orderBy("last_updated", false);
        try {
            fetchedProjects = mProjectDao.query(queryBuilder.prepare());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fetchedProjects;
    }
}
