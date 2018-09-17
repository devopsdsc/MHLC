package red.point.checkpoint.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import red.point.checkpoint.R;
import red.point.checkpoint.api.model.Employee;

public class EmployeeAdapter extends RecyclerView.Adapter<red.point.checkpoint.adapter.EmployeeAdapter.EmployeeViewHolder> {

    private List<Employee> list;
    private static final String TAG = red.point.checkpoint.adapter.EmployeeAdapter.class.getSimpleName();

    public EmployeeAdapter(List<Employee> list) {
        this.list = list;
    }

    @Override
    public red.point.checkpoint.adapter.EmployeeAdapter.EmployeeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new red.point.checkpoint.adapter.EmployeeAdapter.EmployeeViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_employee_list, parent, false));
    }

    @Override
    public void onBindViewHolder(final red.point.checkpoint.adapter.EmployeeAdapter.EmployeeViewHolder holder, int position) {
        Employee employee = list.get(position);
        holder.name.setText(employee.getName());
        holder.email.setText(employee.getEmail());
        if (employee.getAdmin()) {
            holder.role.setText("Admin");
        }
        if (employee.getOwner()) {
            holder.role.setText("Owner");
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class EmployeeViewHolder extends RecyclerView.ViewHolder {
        TextView name, email, role;

        EmployeeViewHolder(View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            email = itemView.findViewById(R.id.email);
            role = itemView.findViewById(R.id.role);
        }
    }
}

