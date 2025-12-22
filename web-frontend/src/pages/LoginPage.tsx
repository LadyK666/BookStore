import React, { useState } from 'react';
import { Button, Card, Col, Form, Input, message, Row, Select, Typography } from 'antd';
import { useNavigate } from 'react-router-dom';
import { http } from '../api/http';

const { Title, Text } = Typography;

type UserType = 'CUSTOMER' | 'ADMIN';

const LoginPage: React.FC = () => {
  const [userType, setUserType] = useState<UserType>('CUSTOMER');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const [form] = Form.useForm();

  const onFinish = async (values: { username: string; password: string }) => {
    try {
      setLoading(true);
      const resp = await http.post('/auth/login', {
        username: values.username.trim(),
        password: values.password,
        userType
      });
      const data = resp.data;
      if (userType === 'CUSTOMER') {
        // 对应 JavaFX: MainApp.showCustomerView(customerId, customerName)
        navigate('/customer', {
          state: {
            customerId: data.customerId,
            customerName: data.customerName
          }
        });
      } else {
        // 对应 JavaFX: MainApp.showAdminView(adminName)
        navigate('/admin', {
          state: {
            adminName: data.adminName
          }
        });
      }
    } catch (e: any) {
      message.error(e?.response?.data?.message || '登录失败');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="full-page-gradient">
      <Row justify="center" style={{ width: '100%' }}>
        <Col xs={24} sm={20} md={16} lg={10} xl={8}>
          <div style={{ textAlign: 'center', marginBottom: 24, color: '#fff' }}>
            <Title level={2} style={{ color: '#fff', marginBottom: 4 }}>
              网上书店管理系统
            </Title>
            <Text style={{ color: 'rgba(255,255,255,0.8)' }}>
              Online Bookstore Management System
            </Text>
          </div>

          <Card
            style={{
              borderRadius: 16,
              boxShadow: '0 18px 45px rgba(0,0,0,0.45)',
              backdropFilter: 'blur(16px)'
            }}
          >
            <Form
              layout="vertical"
              form={form}
              initialValues={{ userType: 'CUSTOMER' }}
              onFinish={onFinish}
            >
              <Form.Item label="用户类型" name="userType">
                <Select
                  value={userType}
                  onChange={(val: UserType) => setLoading(false) || setUserType(val)}
                  options={[
                    { label: '顾客', value: 'CUSTOMER' },
                    { label: '管理员', value: 'ADMIN' }
                  ]}
                />
              </Form.Item>

              <Form.Item
                label="用户名"
                name="username"
                rules={[{ required: true, message: '请输入用户名' }]}
              >
                <Input placeholder="请输入用户名" autoComplete="username" />
              </Form.Item>

              <Form.Item
                label="密码"
                name="password"
                rules={[{ required: true, message: '请输入密码' }]}
              >
                <Input.Password placeholder="请输入密码" autoComplete="current-password" />
              </Form.Item>

              <Form.Item>
                <Button
                  type="primary"
                  htmlType="submit"
                  block
                  size="large"
                  loading={loading}
                >
                  登 录
                </Button>
              </Form.Item>

              <Form.Item>
                <Button
                  type="link"
                  style={{ padding: 0 }}
                  onClick={async () => {
                    const username = form.getFieldValue('username');
                    const password = form.getFieldValue('password');
                    if (!username || !password) {
                      message.warning('请先输入用户名和密码，再尝试快速注册');
                      return;
                    }
                    try {
                      setLoading(true);
                      await http.post('/auth/register', {
                        username: username.trim(),
                        password,
                        // 其他字段后续在“个人信息”中维护，逻辑上等价于 JavaFX 对话框的精简版
                      });
                      message.success('注册成功，请使用该账号登录');
                    } catch (e: any) {
                      message.error(e?.response?.data?.message || '注册失败');
                    } finally {
                      setLoading(false);
                    }
                  }}
                >
                  注册新账号（顾客）
                </Button>
              </Form.Item>
            </Form>

            <div style={{ marginTop: 8, color: '#999', fontSize: 12 }}>
              管理员账号: admin / admin123
            </div>
          </Card>
        </Col>
      </Row>
    </div>
  );
};

export default LoginPage;


