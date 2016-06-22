package org.briarproject.android.blogs;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import org.briarproject.R;
import org.briarproject.android.ActivityComponent;
import org.briarproject.android.blogs.BlogPostAdapter.OnBlogPostClickListener;
import org.briarproject.android.controller.handler.UiResultHandler;
import org.briarproject.android.fragment.BaseFragment;
import org.briarproject.android.util.BriarRecyclerView;
import org.briarproject.api.blogs.Blog;

import java.util.Collection;

import javax.inject.Inject;

import static android.app.Activity.RESULT_OK;
import static android.support.design.widget.Snackbar.LENGTH_LONG;
import static android.support.v4.app.ActivityOptionsCompat.makeCustomAnimation;
import static org.briarproject.android.BriarActivity.GROUP_ID;
import static org.briarproject.android.blogs.BlogActivity.BLOG_NAME;
import static org.briarproject.android.blogs.BlogActivity.REQUEST_WRITE_POST;

public class FeedFragment extends BaseFragment implements
		OnBlogPostClickListener, FeedController.OnBlogPostAddedListener {

	public final static String TAG = FeedFragment.class.getName();

	@Inject
	FeedController feedController;

	private BlogPostAdapter adapter;
	private BriarRecyclerView list;
	private Blog personalBlog = null;

	static FeedFragment newInstance() {
		FeedFragment f = new FeedFragment();

		Bundle args = new Bundle();
		f.setArguments(args);

		return f;
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		setHasOptionsMenu(true);
		View v = inflater.inflate(R.layout.fragment_blog, container, false);

		adapter = new BlogPostAdapter(getActivity(), this);
		list = (BriarRecyclerView) v.findViewById(R.id.postList);
		list.setLayoutManager(new LinearLayoutManager(getActivity()));
		list.setAdapter(adapter);
		list.setEmptyText(R.string.blogs_feed_empty_state);

		return v;
	}

	@Override
	public void injectFragment(ActivityComponent component) {
		component.inject(this);
		feedController.setOnBlogPostAddedListener(this);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// The BlogPostAddedEvent arrives when the controller is not listening
		if (requestCode == REQUEST_WRITE_POST && resultCode == RESULT_OK) {
			// show snackbar informing about successful post creation
			Snackbar s = Snackbar.make(list, R.string.blogs_blog_post_created,
					LENGTH_LONG);
			s.getView().setBackgroundResource(R.color.briar_primary);
			OnClickListener onClick = new OnClickListener() {
				@Override
				public void onClick(View v) {
					list.smoothScrollToPosition(0);
				}
			};
			s.setActionTextColor(ContextCompat
					.getColor(getContext(), R.color.briar_button_positive));
			s.setAction(R.string.blogs_blog_post_scroll_to, onClick);
			s.show();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		feedController
				.loadPersonalBlog(new UiResultHandler<Blog>(getActivity()) {
					@Override
					public void onResultUi(Blog b) {
						personalBlog = b;
					}
				});
	}

	@Override
	public void onResume() {
		super.onResume();
		feedController.onResume();
		feedController.loadPosts(
				new UiResultHandler<Collection<BlogPostItem>>(getActivity()) {
					@Override
					public void onResultUi(Collection<BlogPostItem> posts) {
						if (posts == null) {
							// TODO show error?
						} else if (posts.isEmpty()) {
							list.showData();
						} else {
							adapter.addAll(posts);
						}
					}
				});
	}

	@Override
	public void onPause() {
		super.onPause();
		feedController.onPause();
		// TODO save list position
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.blogs_feed_actions, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_write_blog_post:
				if (personalBlog == null) return false;
				Intent i =
						new Intent(getActivity(), WriteBlogPostActivity.class);
				i.putExtra(GROUP_ID, personalBlog.getId().getBytes());
				i.putExtra(BLOG_NAME, personalBlog.getName());
				ActivityOptionsCompat options =
						makeCustomAnimation(getActivity(),
								android.R.anim.slide_in_left,
								android.R.anim.slide_out_right);
				startActivityForResult(i, REQUEST_WRITE_POST,
						options.toBundle());
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBlogPostAdded(final BlogPostItem post) {
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				adapter.add(post);
				Snackbar s =
						Snackbar.make(list, R.string.blogs_blog_post_received,
								LENGTH_LONG);
				s.getView().setBackgroundResource(R.color.briar_primary);
				OnClickListener onClick = new OnClickListener() {
					@Override
					public void onClick(View v) {
						list.smoothScrollToPosition(0);
					}
				};
				s.setActionTextColor(ContextCompat
						.getColor(getContext(), R.color.briar_button_positive));
				s.setAction(R.string.blogs_blog_post_scroll_to, onClick);
				s.show();
			}
		});
	}

	@Override
	public void onBlogPostClick(int position) {
		// noop
	}

	@Override
	public String getUniqueTag() {
		return TAG;
	}
}
