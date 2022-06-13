package uz.mobiler.lesson58_2.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DividerItemDecoration
import uz.mobiler.lesson58_2.R
import uz.mobiler.lesson58_2.adapters.StudentAdapter
import uz.mobiler.lesson58_2.database.AppDatabase
import uz.mobiler.lesson58_2.database.entity.Group
import uz.mobiler.lesson58_2.database.entity.Student
import uz.mobiler.lesson58_2.databinding.FragmentGroupStudentBinding

private const val ARG_PARAM1 = "group"

class GroupStudentFragment : Fragment() {
    private lateinit var param1: Group
    val appDatabase: AppDatabase by lazy {

        AppDatabase.getInstance(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getSerializable(ARG_PARAM1) as Group
        }
        setHasOptionsMenu(true)
    }

    private lateinit var binding: FragmentGroupStudentBinding
    private lateinit var studentList: ArrayList<Student>
    private lateinit var studentAdapter: StudentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGroupStudentBinding.inflate(inflater, container, false)
        studentList = ArrayList(appDatabase.studentDao().getAllStudent(param1.id))
        binding.apply {
            (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
            val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
            actionBar?.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
            actionBar?.setDisplayShowHomeEnabled(true)
            actionBar?.setDisplayHomeAsUpEnabled(true)
            groupName.text = param1.name
            toolbar.title = param1.name
            studentCount.text = "O’quvchilar soni: " + studentList.size.toString()
            groupTime.text = "Vaqti: " + param1.date
            startGroup.setOnClickListener {
                param1.isOpen = "Closed"
                appDatabase.groupDao().editGroup(param1)
                Navigation.findNavController(binding.root).popBackStack()
            }
            if (studentList.isNotEmpty()) {
                lottie.visibility = View.INVISIBLE
            } else {
                lottie.visibility = View.VISIBLE
            }
            studentAdapter =
                StudentAdapter(studentList, object : StudentAdapter.OnItemClickListener {
                    override fun onItemEdit(student: Student, position: Int) {
                        val bundle = Bundle()
                        bundle.putSerializable("student", student)
                        Navigation.findNavController(binding.root)
                            .navigate(R.id.studentEditFragment, bundle)
                    }

                    override fun onItemDelete(student: Student, position: Int) {
                        AlertDialog.Builder(requireContext()).setCancelable(false)
                            .setMessage("Ushbu o’quvchini o’chirmoqchimisiz ?")
                            .setPositiveButton("Ha") { dialog, _ ->
                                appDatabase.studentDao().deleteStudent(student)
                                studentList.remove(student)
                                if (studentList.isNotEmpty()) {
                                    lottie.visibility = View.INVISIBLE
                                } else {
                                    lottie.visibility = View.VISIBLE
                                }
                                studentAdapter.notifyItemRemoved(position)
                                studentAdapter.notifyItemRangeChanged(position, studentList.size)
                                dialog.dismiss()
                            }
                            .setNegativeButton("Yo’q") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .create()
                            .show()
                    }
                })
            val dividerItemDecoration =
                DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
            rv.addItemDecoration(dividerItemDecoration)
            rv.adapter = studentAdapter
        }
        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: Group) =
            GroupStudentFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_PARAM1, param1)
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add) {
            val bundle = Bundle()
            bundle.putLong("id", param1.id)
            Navigation.findNavController(binding.root).navigate(R.id.studentAddFragment, bundle)
        } else if (item.itemId == android.R.id.home) {
            Navigation.findNavController(binding.root).popBackStack()
        }
        return super.onOptionsItemSelected(item)
    }
}