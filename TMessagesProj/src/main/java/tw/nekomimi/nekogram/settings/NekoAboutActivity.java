package tw.nekomimi.nekogram.settings;

import static org.telegram.messenger.LocaleController.getString;
import static org.telegram.ui.ProfileActivity.sendLogs;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Components.RecyclerListView;

import kotlin.Unit;
import tw.nekomimi.nekogram.DatacenterActivity;
import tw.nekomimi.nekogram.helpers.remote.UpdateHelper;
import tw.nekomimi.nekogram.ui.BottomBuilder;
import tw.nekomimi.nekogram.utils.AlertUtil;
import tw.nekomimi.nekogram.utils.FileUtil;
import xyz.nextalone.nagram.NaConfig;

public class NekoAboutActivity extends BaseNekoSettingsActivity {

    // Row positions
    private int infoHeaderRow;
    private int updatesRow;
    private int toggleLogsRow;
    private int sendLogsRow;
    private int clearLogsRow;
    private int linksHeaderRow;
    private int forkRow;
    private int xChannelRow;
    private int channelRow;
    private int channelTipsRow;
    private int sourceCodeRow;
    private int translationRow;
    private int datacenterStatusRow;
    private int infoLinksDividerRow;
    private int bottomDividerRow;

    @Override
    protected void updateRows() {
        super.updateRows();
        rowCount = 0;

        // Info Section
        infoHeaderRow = rowCount++;
        updatesRow = rowCount++;
        toggleLogsRow = rowCount++;

        // Conditionally add log options
        if (BuildVars.LOGS_ENABLED) {
            sendLogsRow = rowCount++;
            clearLogsRow = rowCount++;
        } else {
            sendLogsRow = -1;
            clearLogsRow = -1;
        }

        infoLinksDividerRow = rowCount++;

        // Links Section
        linksHeaderRow = rowCount++;
        forkRow = rowCount++;
        xChannelRow = rowCount++;
        channelRow = rowCount++;
        channelTipsRow = rowCount++;
        sourceCodeRow = rowCount++;
        translationRow = rowCount++;
        datacenterStatusRow = rowCount++;
        bottomDividerRow = rowCount++;
    }

    @Override
    protected void onItemClick(View view, int position, float x, float y) {
        if (position == updatesRow) {
            onUpdatesClick();
        } else if (position == toggleLogsRow) {
            // Switch log status
            BuildVars.LOGS_ENABLED = BuildVars.DEBUG_VERSION = !BuildVars.LOGS_ENABLED;
            SharedPreferences sharedPreferences = ApplicationLoader.applicationContext.getSharedPreferences("systemConfig", Context.MODE_PRIVATE);
            sharedPreferences.edit().putBoolean("logsEnabled", BuildVars.LOGS_ENABLED).apply();
            
            // Re-calculate row positions and refresh the list
            updateRows();
            if (listAdapter != null) {
                listAdapter.notifyDataSetChanged();
            }
        } else if (position == sendLogsRow) {
            sendLogs(getParentActivity(), false);
        } else if (position == clearLogsRow) {
            clearLogs();
        }
        // Link Rows
        else if (position == forkRow) {
            MessagesController.getInstance(currentAccount).openByUserName("NagramX_Fork", this, 1);
        } else if (position == xChannelRow) {
            MessagesController.getInstance(currentAccount).openByUserName("NagramX", this, 1);
        } else if (position == channelRow) {
            MessagesController.getInstance(currentAccount).openByUserName("nagram_channel", this, 1);
        } else if (position == channelTipsRow) {
            MessagesController.getInstance(currentAccount).openByUserName("NagramTips", this, 1);
        } else if (position == translationRow) {
            Browser.openUrl(getParentActivity(), "https://crowdin.com/project/NagramX");
        } else if (position == sourceCodeRow) {
            Browser.openUrl(getParentActivity(), "https://github.com/Keeperorowner/NagramX_Fork");
        } else if (position == datacenterStatusRow) {
            presentFragment(new DatacenterActivity(0));
        }
    }

    private void onUpdatesClick() {
        if (getParentActivity() == null) {
            return;
        }
        BottomBuilder builder = new BottomBuilder(getParentActivity());
        builder.addTitle(getString(R.string.Updates));

        // Beta version switch
        builder.addCheckItem(getString(R.string.EnableBetaVersion), R.drawable.test_tube_solar, NaConfig.INSTANCE.getEnableBetaVersion().Bool(), false, (cell, isChecked) -> {
            NaConfig.INSTANCE.getEnableBetaVersion().setConfigBool(isChecked);
            return Unit.INSTANCE;
        });

        // Check update on startup switch
        builder.addCheckItem(getString(R.string.CheckUpdateOnStartup), R.drawable.msg_timer, NaConfig.INSTANCE.getCheckUpdateOnStartup().Bool(), false, (cell, isChecked) -> {
            NaConfig.INSTANCE.getCheckUpdateOnStartup().setConfigBool(isChecked);
            return Unit.INSTANCE;
        });

        // Clean updates cache with icon
        builder.addItem(getString(R.string.DebugMenuCleanAppUpdate), R.drawable.msg_clear, (it) -> {
            UpdateHelper.cleanAppUpdate(); //
            return Unit.INSTANCE;
        });

        // Check Update - moved to bottom
        builder.addItem(getString(R.string.CheckUpdate), R.drawable.msg_search_solar,
                (it) -> {
                    Browser.openUrl(getParentActivity(), "tg://update"); //
                    return Unit.INSTANCE;
                });

        showDialog(builder.create());
    }

    private void clearLogs() {
        AlertDialog pro = AlertUtil.showProgress(getParentActivity()); //
        pro.show(); //
        Utilities.globalQueue.postRunnable(() -> {
            FileUtil.delete(AndroidUtilities.getLogsDir()); //
            AndroidUtilities.runOnUIThread(pro::dismiss);
        });
    }

    @Override
    protected BaseListAdapter createAdapter(Context context) {
        return new ListAdapter(context);
    }

    public String getTitle() {
        return getString(R.string.NagranX_About);
    }

    @Override
    protected String getActionBarTitle() {
        return getTitle();
    }

    @Override
    protected boolean hasWhiteActionBar() {
        return false;
    }



    private class ListAdapter extends BaseListAdapter {

        public ListAdapter(Context context) {
            super(context);
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case TYPE_HEADER:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite));
                    break;
                case TYPE_TEXT:
                    view = new TextCell(mContext);
                    view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite));
                    break;
                case TYPE_INFO_PRIVACY:
                    view = new TextInfoPrivacyCell(mContext);
                    view.setBackgroundColor(getThemedColor(Theme.key_windowBackgroundWhite));
                    break;
                default:
                    return super.onCreateViewHolder(parent, viewType);
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            int viewType = holder.getItemViewType();

            switch (viewType) {
                case TYPE_HEADER: {
                    HeaderCell headerCell = (HeaderCell) holder.itemView;
                    if (position == infoHeaderRow) {
                        headerCell.setText(getString(R.string.NagramX_Info));
                    } else if (position == linksHeaderRow) {
                        headerCell.setText(getString(R.string.NagramX_Links));
                    }
                    break;
                }
                case TYPE_TEXT: {
                    TextCell textCell = (TextCell) holder.itemView;
                    if (position == updatesRow) {
                        textCell.setTextAndIcon(getString(R.string.Updates), R.drawable.round_update_white_28, true);
                    } else if (position == toggleLogsRow) {
                        String text = BuildVars.LOGS_ENABLED ? getString(R.string.DebugMenuDisableLogs) : getString(R.string.DebugMenuEnableLogs); //
                        textCell.setTextAndIcon(text, R.drawable.bug_solar, true);
                    } else if (position == sendLogsRow) {
                        textCell.setTextAndIcon(getString(R.string.DebugSendLogs), R.drawable.ic_upward_solar, true);
                    } else if (position == clearLogsRow) {
                        textCell.setTextAndIcon(getString(R.string.DebugClearLogs), R.drawable.msg_clear_solar, true);
                    } else if (position == forkRow) {
                        textCell.setTextAndValue(getString(R.string.Fork), "@NagramX_Fork", true, true);
                    } else if (position == xChannelRow) {
                        textCell.setTextAndValue(getString(R.string.XChannel), "@NagramX", true, true);
                    } else if (position == channelRow) {
                        textCell.setTextAndValue(getString(R.string.OfficialChannel), "@nagram_channel", true, true);
                    } else if (position == channelTipsRow) {
                        textCell.setTextAndValue(getString(R.string.TipsChannel), "@NagramTips", true, true);
                    } else if (position == sourceCodeRow) {
                        textCell.setTextAndValue(getString(R.string.SourceCode), "Github", true, true);
                    } else if (position == translationRow) {
                        textCell.setTextAndValue(getString(R.string.TransSite), "Crowdin", true, true);
                    } else if (position == datacenterStatusRow) {
                        textCell.setText(getString(R.string.DatacenterStatus), true);
                    }
                    break;
                }
                case TYPE_SHADOW: {
                     holder.itemView.setBackground(Theme.getThemedDrawable(mContext, R.drawable.greydivider_bottom, Theme.key_windowBackgroundGrayShadow));
                    break;
                }
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == infoHeaderRow || position == linksHeaderRow) {
                return TYPE_HEADER;
            } else if (position == bottomDividerRow || position == infoLinksDividerRow) {
                return TYPE_SHADOW;
            }
            return TYPE_TEXT;
        }
    }
}