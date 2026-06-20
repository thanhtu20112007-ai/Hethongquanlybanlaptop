package cuoiky;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class AdminFrame extends JFrame {
    private JTextField txtTenSP, txtGiaGoc, txtGiamGia, txtHinhAnh, txtCPU, txtRAM, txtSSD, txtVGA, txtManHinh, txtTonKho;
    private JComboBox<String> cbxHang;
    private JTable tblSanPham;
    private DefaultTableModel tableModel;

    private String maSPDangChon = "";
    private TrangChu trangChu;

    private int maHangDangLoc = 0;
    private HashSet<String> dsMaSPDaChon = new HashSet<>();
    private final String[] dsHangLoc = {"TẤT CẢ", "DELL", "HP", "ASUS", "LENOVO", "ACER", "MACBOOK"};

    public AdminFrame(TrangChu trangChu) {
        this.trangChu = trangChu;
        setTitle("Admin - Quản Lý Sản Phẩm");
        setSize(1100, 780);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(11, 2, 10, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin chi tiết Laptop"));

        txtTenSP = new JTextField(); txtGiaGoc = new JTextField(); txtGiamGia = new JTextField("0");
        txtCPU = new JTextField(); txtRAM = new JTextField(); txtSSD = new JTextField();
        txtVGA = new JTextField(); txtManHinh = new JTextField(); txtTonKho = new JTextField("20");

        cbxHang = new JComboBox<>(new String[]{"Dell", "HP", "Asus", "Lenovo", "Acer", "Macbook"});

        txtHinhAnh = new JTextField();
        JButton btnChonAnh = new JButton("Chọn Ảnh...");
        btnChonAnh.setBackground(new Color(230, 230, 230));
        btnChonAnh.setFocusPainted(false);
        btnChonAnh.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser("C:\\images");
            fileChooser.setFileFilter(new FileNameExtensionFilter("Hình ảnh (JPG, PNG)", "jpg", "png", "jpeg"));
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                txtHinhAnh.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });
        JPanel pnlHinhAnh = new JPanel(new BorderLayout(5, 0));
        pnlHinhAnh.add(txtHinhAnh, BorderLayout.CENTER);
        pnlHinhAnh.add(btnChonAnh, BorderLayout.EAST);

        formPanel.add(new JLabel("Tên Laptop: (*)")); formPanel.add(txtTenSP);
        formPanel.add(new JLabel("Hãng:")); formPanel.add(cbxHang);
        formPanel.add(new JLabel("Giá gốc (VNĐ): (*)")); formPanel.add(txtGiaGoc);
        formPanel.add(new JLabel("Giảm giá (%) hoặc Giá mới:")); formPanel.add(txtGiamGia);
        formPanel.add(new JLabel("Số lượng tồn kho:")); formPanel.add(txtTonKho);
        formPanel.add(new JLabel("Link ảnh / File:")); formPanel.add(pnlHinhAnh);
        formPanel.add(new JLabel("CPU:")); formPanel.add(txtCPU);
        formPanel.add(new JLabel("RAM:")); formPanel.add(txtRAM);
        formPanel.add(new JLabel("SSD:")); formPanel.add(txtSSD);
        formPanel.add(new JLabel("VGA:")); formPanel.add(txtVGA);
        formPanel.add(new JLabel("Màn hình:")); formPanel.add(txtManHinh);
        add(formPanel, BorderLayout.NORTH);

        JPanel tableWrapper = new JPanel(new BorderLayout());

        JPanel pnlLocHang = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlLocHang.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        for (int i = 0; i < dsHangLoc.length; i++) {
            JButton btnLoc = new JButton(dsHangLoc[i]);
            btnLoc.setFont(new Font("Arial", Font.BOLD, 12));
            btnLoc.setBackground(i == 0 ? new Color(0, 102, 204) : Color.WHITE);
            btnLoc.setForeground(i == 0 ? Color.WHITE : Color.BLACK);

            final int indexLoc = i;
            btnLoc.addActionListener(e -> {
                maHangDangLoc = indexLoc;
                for (Component comp : pnlLocHang.getComponents()) {
                    if (comp instanceof JButton) {
                        ((JButton) comp).setBackground(Color.WHITE);
                        ((JButton) comp).setForeground(Color.BLACK);
                    }
                }
                btnLoc.setBackground(new Color(0, 102, 204));
                btnLoc.setForeground(Color.WHITE);

                loadDataVaoBang();
            });
            pnlLocHang.add(btnLoc);
        }

        String[] columns = {"Chọn", "STT", "Mã SP", "Tên SP", "Giá Gốc", "Tồn Kho", "RAM"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 0 ? Boolean.class : Object.class;
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
            @Override
            public void setValueAt(Object aValue, int row, int column) {
                super.setValueAt(aValue, row, column);
                if (column == 0) {
                    String maSP = getValueAt(row, 2).toString();
                    if ((Boolean) aValue) {
                        dsMaSPDaChon.add(maSP);
                    } else {
                        dsMaSPDaChon.remove(maSP);
                    }
                }
            }
        };
        tblSanPham = new JTable(tableModel);

        tblSanPham.getColumnModel().getColumn(0).setPreferredWidth(40);
        tblSanPham.getColumnModel().getColumn(1).setPreferredWidth(40);
        tblSanPham.getColumnModel().getColumn(2).setPreferredWidth(100);

        tblSanPham.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = tblSanPham.getSelectedRow();
                if (row >= 0) {
                    maSPDangChon = tableModel.getValueAt(row, 2).toString();
                    hienThiChiTietLenForm(maSPDangChon);
                }
            }
        });

        JScrollPane scrollTable = new JScrollPane(tblSanPham);
        scrollTable.setBorder(BorderFactory.createTitledBorder("Danh sách Laptop"));

        JPanel pnlDuoiBang = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        pnlDuoiBang.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JButton btnXoaDaTich = new JButton("XÓA CÁC Ô ĐÃ TÍCH");
        btnXoaDaTich.setBackground(new Color(220, 53, 69)); btnXoaDaTich.setForeground(Color.WHITE); btnXoaDaTich.setFont(new Font("Arial", Font.BOLD, 12));
        btnXoaDaTich.addActionListener(e -> xuLyXoaDaTich());

        JButton btnXoaHangNay = new JButton("XÓA TẤT CẢ SP HÃNG NÀY");
        btnXoaHangNay.setBackground(new Color(204, 0, 0)); btnXoaHangNay.setForeground(Color.WHITE); btnXoaHangNay.setFont(new Font("Arial", Font.BOLD, 12));
        btnXoaHangNay.addActionListener(e -> xuLyXoaHangNay());

        pnlDuoiBang.add(btnXoaDaTich);
        pnlDuoiBang.add(btnXoaHangNay);

        tableWrapper.add(pnlLocHang, BorderLayout.NORTH);
        tableWrapper.add(scrollTable, BorderLayout.CENTER);
        tableWrapper.add(pnlDuoiBang, BorderLayout.SOUTH);
        add(tableWrapper, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnThem = new JButton("THÊM MỚI"); btnThem.setBackground(new Color(0, 153, 51)); btnThem.setForeground(Color.WHITE);
        JButton btnSua = new JButton("CẬP NHẬT"); btnSua.setBackground(new Color(255, 153, 0)); btnSua.setForeground(Color.WHITE);
        JButton btnXoa = new JButton("XÓA 1 SP"); btnXoa.setBackground(new Color(204, 0, 0)); btnXoa.setForeground(Color.WHITE);
        JButton btnXoaTatCa = new JButton("XÓA NHIỀU / ALL"); btnXoaTatCa.setBackground(Color.BLACK); btnXoaTatCa.setForeground(Color.WHITE);
        JButton btnTaoAo = new JButton("TẠO 50 SP ẢO"); btnTaoAo.setBackground(new Color(40, 167, 69)); btnTaoAo.setForeground(Color.WHITE);
        JButton btnLichSuGD = new JButton("LỊCH SỬ"); btnLichSuGD.setBackground(new Color(72, 44, 115)); btnLichSuGD.setForeground(Color.WHITE);
        JButton btnThongKe = new JButton("DOANH THU"); btnThongKe.setBackground(new Color(0, 102, 204)); btnThongKe.setForeground(Color.WHITE);
        JButton btnClear = new JButton("LÀM TRỐNG");

        btnThem.addActionListener(e -> xuLyThem());
        btnSua.addActionListener(e -> xuLySua());
        btnXoa.addActionListener(e -> xuLyXoa());
        btnXoaTatCa.addActionListener(e -> xuLyXoaNhieuHoacTatCa());
        btnClear.addActionListener(e -> lamTrongForm());
        btnLichSuGD.addActionListener(e -> new LichSuGiaoDichFrame(true, "Admin").setVisible(true));
        btnThongKe.addActionListener(e -> new ThongKeFrame().setVisible(true));
        btnTaoAo.addActionListener(e -> tao50SanPhamNgauNhien());

        btnPanel.add(btnThem); btnPanel.add(btnSua); btnPanel.add(btnXoa);
        btnPanel.add(btnXoaTatCa);
        btnPanel.add(btnTaoAo);
        btnPanel.add(btnLichSuGD); btnPanel.add(btnThongKe); btnPanel.add(btnClear);
        add(btnPanel, BorderLayout.SOUTH);

        loadDataVaoBang();
    }

    private boolean kiemTraHopLe() {
        if (txtTenSP.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tên Laptop không được để trống!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtTenSP.requestFocus();
            return false;
        }
        String strGiaGoc = txtGiaGoc.getText().replaceAll("[^0-9]", "");
        if (strGiaGoc.isEmpty() || Double.parseDouble(strGiaGoc) <= 0) {
            JOptionPane.showMessageDialog(this, "Giá gốc phải là một số lớn hơn 0!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            txtGiaGoc.requestFocus();
            return false;
        }
        return true;
    }

    // --- ĐÃ NÂNG CẤP: THUẬT TOÁN ĐỌC ẢNH THEO THƯ MỤC HÃNG ---
    private void tao50SanPhamNgauNhien() {
        int confirm = JOptionPane.showConfirmDialog(this, "Hệ thống sẽ tạo 50 sản phẩm ngẫu nhiên.\nẢnh sẽ được nạp tự động không trùng lặp từ các thư mục con trong C:\\images.\nTiếp tục?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        String[] brands = {"Dell", "HP", "Asus", "Lenovo", "Acer", "Macbook"};

        // Tạo "Từ điển" chứa danh sách ảnh riêng biệt cho từng hãng
        java.util.Map<String, List<String>> danhSachAnhTheoHang = new java.util.HashMap<>();
        java.util.Map<String, Integer> indexAnhTheoHang = new java.util.HashMap<>();

        File rootDir = new File("C:\\images");
        if (rootDir.exists() && rootDir.isDirectory()) {
            for (String hang : brands) {
                List<String> anhCuaHang = new ArrayList<>();
                // Tìm đúng thư mục con mang tên hãng (ví dụ: C:\images\dell)
                File folderHang = new File(rootDir, hang.toLowerCase());
                if (folderHang.exists() && folderHang.isDirectory()) {
                    File[] files = folderHang.listFiles();
                    if (files != null) {
                        for (File f : files) {
                            String name = f.getName().toLowerCase();
                            if (f.isFile() && (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg"))) {
                                anhCuaHang.add(f.getAbsolutePath());
                            }
                        }
                    }
                }
                // Xào bài danh sách ảnh của hãng này để lấy ngẫu nhiên không trùng
                if (!anhCuaHang.isEmpty()) {
                    java.util.Collections.shuffle(anhCuaHang);
                }
                danhSachAnhTheoHang.put(hang, anhCuaHang);
                indexAnhTheoHang.put(hang, 0); // Đánh dấu bắt đầu bốc từ ảnh số 0
            }
        } else {
            JOptionPane.showMessageDialog(this, "Cảnh báo: Không tìm thấy thư mục C:\\images.", "Thiếu ảnh", JOptionPane.WARNING_MESSAGE);
        }

        String[] cpus = {"Intel Core i5 12400F", "Intel Core i7 13700H", "AMD Ryzen 5 5600H", "AMD Ryzen 7 6800H", "Apple M2"};
        String[] rams = {"8GB DDR4 3200MHz", "16GB DDR4 3200MHz", "16GB DDR5 4800MHz", "32GB DDR5 5600MHz"};
        String[] ssds = {"256GB PCIe NVMe", "512GB PCIe NVMe", "1TB PCIe Gen4"};
        String[] vgas = {"Intel Iris Xe Graphics", "NVIDIA RTX 3050 4GB", "NVIDIA RTX 4060 8GB", "AMD Radeon Graphics"};
        String[] manHinhs = {"14 inch FHD IPS", "15.6 inch FHD 144Hz", "16 inch 2.5K 165Hz"};

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO SanPham (MaSP, TenSP, MaHang, GiaGoc, PhanTramGiam, HinhAnh, CPU, RAM, SSD, VGA, ManHinh, SoLuongTonKho) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            Random rand = new Random();

            for (int i = 0; i < 50; i++) {
                int hangIndex = rand.nextInt(brands.length);
                String tenHang = brands[hangIndex];
                String tenSP = tenHang + " Laptop Mẫu " + rand.nextInt(9999);
                double giaGoc = 10000000 + rand.nextInt(40) * 1000000;
                int giamGia = rand.nextInt(25);
                int tonKho = 10 + rand.nextInt(91);

                // Lấy ảnh tuần tự không trùng lặp từ đúng thư mục của hãng đó
                String hinhAnhChon = "";
                List<String> anhCuaHang = danhSachAnhTheoHang.get(tenHang);
                if (anhCuaHang != null && !anhCuaHang.isEmpty()) {
                    int currentIndex = indexAnhTheoHang.get(tenHang);
                    hinhAnhChon = anhCuaHang.get(currentIndex % anhCuaHang.size()); // Modulo để quay vòng nếu lỡ bốc hết số ảnh
                    indexAnhTheoHang.put(tenHang, currentIndex + 1);
                }

                ps.setString(1, taoMaSanPhamNgauNhien());
                ps.setString(2, tenSP);
                ps.setInt(3, hangIndex + 1);
                ps.setDouble(4, giaGoc);
                ps.setInt(5, giamGia);
                ps.setString(6, hinhAnhChon);
                ps.setString(7, cpus[rand.nextInt(cpus.length)]);
                ps.setString(8, rams[rand.nextInt(rams.length)]);
                ps.setString(9, ssds[rand.nextInt(ssds.length)]);
                ps.setString(10, vgas[rand.nextInt(vgas.length)]);
                ps.setString(11, manHinhs[rand.nextInt(manHinhs.length)]);
                ps.setInt(12, tonKho);

                ps.addBatch();
            }
            ps.executeBatch();

            JOptionPane.showMessageDialog(this, "Tuyệt vời! Đã bơm thành công 50 sản phẩm mẫu (Hình ảnh được phân phối chuẩn theo hãng)!");
            capNhatGiaoDien();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi tạo dữ liệu ảo: " + ex.getMessage(), "Báo lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String taoMaSanPhamNgauNhien() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < 10; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    private void loadDataVaoBang() {
        tableModel.setRowCount(0);
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT MaSP, TenSP, GiaGoc, SoLuongTonKho, RAM FROM SanPham";
            if (maHangDangLoc > 0) {
                sql += " WHERE MaHang = " + maHangDangLoc;
            }
            ResultSet rs = conn.prepareStatement(sql).executeQuery();
            int stt = 1;
            while (rs.next()) {
                String maSP = rs.getString("MaSP");
                boolean isSelected = dsMaSPDaChon.contains(maSP);
                tableModel.addRow(new Object[]{isSelected, stt++, maSP, rs.getString("TenSP"), rs.getDouble("GiaGoc"), rs.getInt("SoLuongTonKho"), rs.getString("RAM")});
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void hienThiChiTietLenForm(String maSP) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM SanPham WHERE MaSP = ?");
            ps.setString(1, maSP);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                txtTenSP.setText(rs.getString("TenSP")); cbxHang.setSelectedIndex(rs.getInt("MaHang") - 1);
                txtGiaGoc.setText(String.format("%.0f", rs.getDouble("GiaGoc"))); txtGiamGia.setText(String.valueOf(rs.getInt("PhanTramGiam")));
                txtTonKho.setText(String.valueOf(rs.getInt("SoLuongTonKho")));
                txtHinhAnh.setText(rs.getString("HinhAnh")); txtCPU.setText(rs.getString("CPU"));
                txtRAM.setText(rs.getString("RAM")); txtSSD.setText(rs.getString("SSD"));
                txtVGA.setText(rs.getString("VGA")); txtManHinh.setText(rs.getString("ManHinh"));
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void xuLyThem() {
        if (!kiemTraHopLe()) return;

        try (Connection conn = DatabaseConnection.getConnection()) {
            String maSPMoi = taoMaSanPhamNgauNhien();
            PreparedStatement ps = conn.prepareStatement("INSERT INTO SanPham (MaSP, TenSP, MaHang, GiaGoc, PhanTramGiam, HinhAnh, CPU, RAM, SSD, VGA, ManHinh, SoLuongTonKho) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            ps.setString(1, maSPMoi);
            gắnDữLiệuVàoQuery(ps, 2);
            if (ps.executeUpdate() > 0) { JOptionPane.showMessageDialog(this, "Đã THÊM thành công!\nMã SP: " + maSPMoi); capNhatGiaoDien(); }
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage()); }
    }

    private void xuLySua() {
        if (maSPDangChon.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 sản phẩm trong bảng để cập nhật!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!kiemTraHopLe()) return;

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("UPDATE SanPham SET TenSP=?, MaHang=?, GiaGoc=?, PhanTramGiam=?, HinhAnh=?, CPU=?, RAM=?, SSD=?, VGA=?, ManHinh=?, SoLuongTonKho=? WHERE MaSP=?");
            gắnDữLiệuVàoQuery(ps, 1);
            ps.setString(12, maSPDangChon);
            if (ps.executeUpdate() > 0) { JOptionPane.showMessageDialog(this, "Đã CẬP NHẬT thành công!"); capNhatGiaoDien(); }
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage()); }
    }

    private void xuLyXoa() {
        if (maSPDangChon.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn 1 sản phẩm trong bảng để xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Chắc chắn xóa sản phẩm " + maSPDangChon + "?", "Cảnh báo", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement("DELETE FROM SanPham WHERE MaSP = ?");
                ps.setString(1, maSPDangChon);
                if (ps.executeUpdate() > 0) {
                    dsMaSPDaChon.remove(maSPDangChon);
                    JOptionPane.showMessageDialog(this, "Đã XÓA thành công!");
                    capNhatGiaoDien();
                }
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Lỗi: Không thể xóa vì sản phẩm này đã tồn tại trong Lịch sử Đơn Hàng!\nChi tiết: " + ex.getMessage(), "Lỗi SQL", JOptionPane.ERROR_MESSAGE); }
        }
    }

    private void xuLyXoaDaTich() {
        if (dsMaSPDaChon.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Bạn chưa tích chọn sản phẩm nào!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa " + dsMaSPDaChon.size() + " sản phẩm đã tích?", "Xác nhận xóa nhiều", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                PreparedStatement ps = conn.prepareStatement("DELETE FROM SanPham WHERE MaSP = ?");
                for (String maSP : dsMaSPDaChon) {
                    ps.setString(1, maSP);
                    ps.addBatch();
                }
                ps.executeBatch();
                dsMaSPDaChon.clear();
                JOptionPane.showMessageDialog(this, "Đã xóa thành công các sản phẩm được tích chọn!");
                capNhatGiaoDien();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: Một số sản phẩm không thể xóa do khách hàng đã mua!\nChi tiết: " + ex.getMessage(), "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void xuLyXoaHangNay() {
        if (maHangDangLoc == 0) {
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn đang ở chế độ hiển thị 'TẤT CẢ'. \nQuá trình này sẽ XÓA SẠCH toàn bộ sản phẩm của MỌI HÃNG. Chắc chắn chứ?", "Cảnh báo nguy hiểm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    conn.prepareStatement("DELETE FROM SanPham").executeUpdate();
                    dsMaSPDaChon.clear();
                    JOptionPane.showMessageDialog(this, "Đã dọn SẠCH toàn bộ sản phẩm trong hệ thống!");
                    capNhatGiaoDien();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi: Không thể xóa tất cả vì một số sản phẩm đang nằm trong đơn hàng của khách!\nChi tiết: " + ex.getMessage(), "Lỗi dữ liệu", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            String tenHang = dsHangLoc[maHangDangLoc];
            int confirm = JOptionPane.showConfirmDialog(this, "Chắc chắn xóa TẤT CẢ sản phẩm của hãng " + tenHang + "? \nCác hãng khác sẽ được giữ nguyên.", "Xác nhận xóa hãng", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    PreparedStatement ps = conn.prepareStatement("DELETE FROM SanPham WHERE MaHang = ?");
                    ps.setInt(1, maHangDangLoc);
                    ps.executeUpdate();
                    dsMaSPDaChon.clear();
                    JOptionPane.showMessageDialog(this, "Đã xóa SẠCH các sản phẩm thuộc hãng " + tenHang + "!");
                    capNhatGiaoDien();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi dữ liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void xuLyXoaNhieuHoacTatCa() {
        if (!dsMaSPDaChon.isEmpty()) {
            xuLyXoaDaTich();
        } else {
            int confirm = JOptionPane.showConfirmDialog(this, "Quá trình này sẽ xóa tất cả sản phẩm, bạn có chắc chắn?", "Cảnh báo nguy hiểm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DatabaseConnection.getConnection()) {
                    PreparedStatement ps = conn.prepareStatement("DELETE FROM SanPham");
                    ps.executeUpdate();
                    dsMaSPDaChon.clear();
                    JOptionPane.showMessageDialog(this, "Đã dọn SẠCH toàn bộ sản phẩm trong hệ thống!");
                    capNhatGiaoDien();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Lỗi dữ liệu: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void gắnDữLiệuVàoQuery(PreparedStatement ps, int startIndex) throws Exception {
        ps.setString(startIndex, txtTenSP.getText().trim());
        ps.setInt(startIndex + 1, cbxHang.getSelectedIndex() + 1);
        String strGiaGoc = txtGiaGoc.getText().replaceAll("[^0-9]", "");
        String strGiamGia = txtGiamGia.getText().replaceAll("[^0-9]", "");
        String strTonKho = txtTonKho.getText().replaceAll("[^0-9]", "");

        double giaGoc = strGiaGoc.isEmpty() ? 0 : Double.parseDouble(strGiaGoc);
        int giamGia = strGiamGia.isEmpty() ? 0 : Integer.parseInt(strGiamGia);
        int tonKho = strTonKho.isEmpty() ? 0 : Integer.parseInt(strTonKho);

        if (giamGia > 100 && giaGoc > 0) giamGia = (int) Math.round(((giaGoc - giamGia) / giaGoc) * 100);

        ps.setDouble(startIndex + 2, giaGoc);
        ps.setInt(startIndex + 3, giamGia);
        ps.setString(startIndex + 4, txtHinhAnh.getText().trim());
        ps.setString(startIndex + 5, txtCPU.getText().trim());
        ps.setString(startIndex + 6, txtRAM.getText().trim());
        ps.setString(startIndex + 7, txtSSD.getText().trim());
        ps.setString(startIndex + 8, txtVGA.getText().trim());
        ps.setString(startIndex + 9, txtManHinh.getText().trim());
        ps.setInt(startIndex + 10, tonKho);
    }

    private void capNhatGiaoDien() {
        loadDataVaoBang();
        lamTrongForm();
        if (trangChu != null) trangChu.loadDuLieuTuSQL();
    }

    private void lamTrongForm() {
        txtTenSP.setText(""); txtGiaGoc.setText(""); txtGiamGia.setText("0"); txtTonKho.setText("20");
        txtHinhAnh.setText(""); txtCPU.setText(""); txtRAM.setText(""); txtSSD.setText("");
        txtVGA.setText(""); txtManHinh.setText(""); maSPDangChon = "";
    }
}
